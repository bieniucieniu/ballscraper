
# build 
use amper to build and run applications under nix and devenv.
project is only compiled to native target except for development were jvm is intended
multiplatform modules should be laydown:
- <module-name>
    - src/
        - ..
    - targets/
        - <module-name>-linux/
            - ..
        - <module-name>-macos/
            - ..
        - <module-name>-windows/
            - ..
        - <module-name>-jvm/
            - ..
# project structure
keep it simple, use flat file structure 
nest only if there is clear modularity
don't use maven like structure, use flat file structure; amper allows it

# questions
    - ask user question when starting editing each amper defined module
