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
        body_buffer: ?[]u8 = null,
        arena: std.heap.ArenaAllocator,

        pub fn deinit(self: *HttpResponse) void {
            // req.deinit() ensures the connection is properly returned to the pool
            self.req.deinit();
            self.arena.deinit();
        }

        /// Reads the entire response body as a raw byte slice.
        /// The memory is owned by the response's arena and is valid until deinit().
        pub fn text(self: *HttpResponse) ![]u8 {
            if (self.body_buffer) |b| return b;

            const allocator = self.arena.allocator();
            const decompress_buffer: []u8 = switch (self.res.head.content_encoding) {
                .identity => &[_]u8{},
                .zstd => try allocator.alloc(u8, std.compress.zstd.default_window_len),
                .deflate, .gzip => try allocator.alloc(u8, std.compress.flate.max_window_len),
                .compress => return error.UnsupportedCompressionMethod,
            };
            defer allocator.free(decompress_buffer);

            var transfer_buffer: [64]u8 = undefined;
            var decompress: http.Decompress = undefined;
            const reader = self.res.readerDecompressing(&transfer_buffer, &decompress, decompress_buffer);

            const body_buffer = try reader.readAllAlloc(allocator, 10 * 1024 * 1024); // 10MB limit
            self.body_buffer = body_buffer;

            return body_buffer;
        }

        /// Retrieves and parses the body into type T.
        /// Returns std.json.Parsed(T) which contains the value and its own arena.
        /// Caller must call .deinit() on the returned Parsed(T) object.
        pub fn body(self: *HttpResponse, comptime T: type) !std.json.Parsed(T) {
            const raw_body = try self.text();
            // Use the base allocator for the parsed result so it can be managed independently
            return std.json.parseFromSlice(T, self.arena.child_allocator, raw_body, .{
                .ignore_unknown_fields = true,
            });
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

        var arena = std.heap.ArenaAllocator.init(self.allocator);
        errdefer arena.deinit();
        const allocator = arena.allocator();

        // Allocate Request on arena so it's cleaned up automatically
        const req = try allocator.create(http.Client.Request);

        req.* = try self.client.request(.GET, uri, .{
            .headers = .{ .connection = .default },
            .extra_headers = opt.headers,
        });
        errdefer req.deinit();

        try req.sendBodiless();

        const redirect_buf = try allocator.alloc(u8, 8192);
        const res = try req.receiveHead(redirect_buf);
        if (res.head.status != .ok) return error.HttpError;

        return HttpResponse{
            .req = req,
            .res = res,
            .redirect_buffer = redirect_buf,
            .arena = arena,
        };
    }

    pub fn post(self: *HttpClient, url: []const u8, body_obj: anytype, opt: HttpClientPostOptions) !HttpResponse {
        const uri = try std.Uri.parse(url);

        var arena = std.heap.ArenaAllocator.init(self.allocator);
        errdefer arena.deinit();
        const allocator = arena.allocator();

        const req = try allocator.create(http.Client.Request);

        req.* = try self.client.request(.POST, uri, .{
            .headers = .{ .content_type = .{ .override = "application/json" } },
            .extra_headers = opt.headers,
        });
        errdefer req.deinit();

        const body_json = try std.json.stringifyAlloc(allocator, body_obj, .{});
        try req.sendBodyComplete(body_json);

        const redirect_buf = try allocator.alloc(u8, 8192);
        const res = try req.receiveHead(redirect_buf);
        if (res.head.status != .ok) return error.HttpError;

        return HttpResponse{
            .req = req,
            .res = res,
            .redirect_buffer = redirect_buf,
            .arena = arena,
        };
    }
};
