const std = @import("std");
const http = std.http;
const Allocator = std.mem.Allocator;
const Io = std.Io;

pub const HttpClient = struct {
    client: http.Client,
    allocator: Allocator,
    io: Io,

    pub fn init(allocator: Allocator, io: Io) HttpClient {
        return .{
            // The new std.http.Client expects the allocator and the io interface
            .client = .{
                .allocator = allocator,
                .io = io,
            },
            .allocator = allocator,
            .io = io,
        };
    }

    pub fn deinit(self: *HttpClient) void {
        self.client.deinit();
    }

    pub const HttpResponse = struct {
        req: *http.Client.Request,
        res: http.Client.Response,
        redirect_buffer: []u8,
        allocator: Allocator,
        arena: ?std.heap.ArenaAllocator = null,

        pub fn deinit(self: *HttpResponse) void {
            if (self.arena) |*a| {
                a.deinit();
            }
            // Calling req.deinit() will discard the rest of the body
            // so the connection can be returned to the connection pool
            self.req.deinit();
            self.allocator.destroy(self.req);
            self.allocator.free(self.redirect_buffer);
        }

        pub fn body(self: *HttpResponse, comptime T: type) !T {
            if (self.arena != null) return error.BodyAlreadyRead;

            self.arena = std.heap.ArenaAllocator.init(self.allocator);
            errdefer {
                self.arena.?.deinit();
                self.arena = null;
            }
            const arena_allocator = self.arena.?.allocator();

            const decompress_buffer: []u8 = switch (self.res.head.content_encoding) {
                .identity => &[_]u8{},
                .zstd => try arena_allocator.alloc(u8, std.compress.zstd.default_window_len),
                .deflate, .gzip => try arena_allocator.alloc(u8, std.compress.flate.max_window_len),
                .compress => return error.UnsupportedCompressionMethod,
            };

            // The transfer buffer must be preserved for the duration of the reading
            var transfer_buffer: [64]u8 = undefined;
            var decompress: http.Decompress = undefined;
            const reader = self.res.readerDecompressing(&transfer_buffer, &decompress, decompress_buffer);

            const raw_body = reader.readAllAlloc(arena_allocator, 10 * 1024 * 1024) catch |err| switch (err) {
                error.ReadFailed => return self.res.bodyErr().?,
                else => |e| return e,
            };

            const parsed = try std.json.parseFromSlice(T, arena_allocator, raw_body, .{
                .ignore_unknown_fields = true,
            });
            return parsed.value;
        }
    };

    pub const HttpClientGetOptions = struct {
        headers: []const http.Header = &.{},
    };

    pub const HttpClientPostOptions = struct {
        headers: []const http.Header = &.{},
    };

    pub fn get(self: *HttpClient, url: []const u8, opt: HttpClientGetOptions) !HttpResponse {
        const uri = try std.Uri.parse(url);

        // We allocate the Request on the heap so its memory address is stable
        // because receiveHead() returns a Response containing a pointer to it.
        const req = try self.allocator.create(http.Client.Request);
        errdefer self.allocator.destroy(req);

        req.* = try self.client.request(.GET, uri, .{
            .headers = .{ .content_type = .{ .override = "application/json" } },
            .extra_headers = opt.headers,
        });
        errdefer req.deinit();

        try req.sendBodiless();

        const redirect_buf = try self.allocator.alloc(u8, 8192);
        errdefer self.allocator.free(redirect_buf);

        const res = try req.receiveHead(redirect_buf);
        if (res.head.status != .ok) return error.HttpError;

        return HttpResponse{
            .req = req,
            .res = res,
            .redirect_buffer = redirect_buf,
            .allocator = self.allocator,
        };
    }

    pub fn post(self: *HttpClient, url: []const u8, body_obj: anytype, opt: HttpClientPostOptions) !HttpResponse {
        const uri = try std.Uri.parse(url);

        const req = try self.allocator.create(http.Client.Request);
        errdefer self.allocator.destroy(req);

        req.* = try self.client.request(.POST, uri, .{
            .headers = .{ .content_type = .{ .override = "application/json" } },
            .extra_headers = opt.headers,
        });
        errdefer req.deinit();

        const body_json = try std.json.stringifyAlloc(self.allocator, body_obj, .{});
        defer self.allocator.free(body_json);

        try req.sendBodyComplete(body_json);

        const redirect_buf = try self.allocator.alloc(u8, 8192);
        errdefer self.allocator.free(redirect_buf);

        const res = try req.receiveHead(redirect_buf);
        if (res.head.status != .ok) return error.HttpError;

        return HttpResponse{
            .req = req,
            .res = res,
            .redirect_buffer = redirect_buf,
            .allocator = self.allocator,
        };
    }
};
