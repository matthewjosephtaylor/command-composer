# Command Composer

A composition tool that allows the user to pick and choose commands from a variety of docker images to be used in their host environment, just as they would any other installed application/command.

The Primary purpose is to allow easy mixing and matching of commands similar to how
one mixes and matches services with docker-compose.

Put another way: commands are merely functions, that act on input and produce output.
There is no reason why one shouldn't be able to _easily_ assemble a set of commands,
use them, and then re-assemble another set later on.

Most docker composition tools focus entirely on the composition from the point
of view of long running networked _services_.  This tool focuses on composition
from a command perspective.

This tool allows one to:
-  Pluck out a wide variety of tools from the universe of anything that runs on some variant of linux. Use them and then abandon them, with out the hassle of having to go through a tedious install/uninstall cycle, and maybe messing up something on your host OS.
- Isolate commands to see _only_ the directory structures they need to. Safely use software and not care too much about where it came from, or what crazy stuff it is junking up your home folder with, because you can now _choose_ easily and exactly what folders the command has access to.
- Create a specific grouping of commands (a _composition_ if one will :) ) that interact with one another and can easily be saved for later or shared with others.

## Installation
Assumes a unix-like environment with a reasonably modern version of docker installed...and that is it!

How this was accomplished (you'll note the source is in Java but you don't need Java installed to build it) is an excellent example of the power of command composition in action. Look inside the [build](build) file and the [/compiler](/compiler) directory to see how it works.


1. clone this project
2. ./build
3. edit your .bash_profile (or equivalent) and append the following:
```
alias command-composer='docker run -it --rm -w="${PWD}" -e "HOME=${HOME}" -v "${PWD}:${PWD}" command-composer'
```

## Examples
Creating a 'dot file' that can be sourced

`command-composer -p java9:java openjdk:9; . .composed-commands`

This will create a command `java9` that can be used like:

```
user@somewhere:$ java9 -version
Unable to find image 'openjdk:9' locally
9: Pulling from library/openjdk
0d62cc759168: Pull complete
d1a452a4ba8c: Pull complete
b1fbfa5a51b3: Pull complete
f5c6d4dcde8a: Pull complete
056b597bb044: Pull complete
4cb44b26812e: Pull complete
b93808fe9b2f: Pull complete
Digest: sha256:ab7df2396e1af08c52ad45be041071679f46410a8f1af6c6b4a256bfdaf384c5
Status: Downloaded newer image for openjdk:9
openjdk version "9-Debian"
OpenJDK Runtime Environment (build 9-Debian+0-9b161-1)
OpenJDK 64-Bit Server VM (build 9-Debian+0-9b161-1, mixed mode)
```

Setting up aliases without a 'dot file'

NOTE: This doesn't work with bash 3.x (which is what, for instance, macosx is stuck on due to License issues (grr...))

`source <(command-composer -f ./ancient-ruby-env.yml)`


Example command-compose.yml

```
name: "example"
command-type: ALIAS
executables-dir: "/home/alice/composed-environments/example/bin"
persist: false

commands:
  java9:
    can-see: "-2"
    also-see:
      - "/var/data/pgdata"
      - "/var/output/logs"
    hot: true
    stateful: false
    image: "java:9"
    container-command: "java"

  yarn:
    can-see: HOME
    image: "my-special-yarn-image:latest"
```
By default a file named 'command-compose.yml' in the current directory is read when `command-composer` is called with no arguments.

Pretty much everything in the yaml file is optional with reasonable defaults.

## Yaml Documentation

- can-see
  -  HOME = only home directory
  -  CWD = (default) only current working directory
  -  NOTHING = can't see any host directories
  -  -X = only X directories up from current working directory (-0 would be the same as CWD, -1 would be one directory up from CWD, ...)

- also-see
  - absolute path to a directory outside of the user's home directory (careful, if you use something like `/` or `/usr/bin` things will likely break)

- persist
  - save an environment file that can be sourced into a shell via `source .name` (default name is `.composed-commands`)

- executables-dir
  - directory to save executable commands that link to docker commands (as apposed to shell aliases) that can be put in a PATH or executed outside of a shell environment.

- command-type
  - ALIAS = environment to use shell aliases
  - EXECUTABLE = environment to use executable commands
- name
  - name of the command group environment (used in the name of the environment file to be sourced)


## Commandline Usage
```
Usage: command-composer [options] command-name[:container-command-name] [image-name] [can-see] [add-dir ...]
  Options:
    -e, --executables
      Output exectutables (default: shell aliases)
      Default: false
    -f, --file FILE
      Specify an alternate compose file (default: command-compose.yml)
    -h, --help
      Print Help
    -n, --name
      Name of envionment (default: .command-commpose). Setting this will force --persist
    -o, --out
      Directory to output executable commands (default: .). Setting this will force --executables
    -p, --persist
      Persist composition to default envornment in current working directory named composed-commands
    -v, --verbose
      Verbose output
      Default: false
    --version
      Print version information
      Default: false
```



## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## TODO
- ~~Get rid of java/maven build dependancies (use docker images)~~ FIXED (we are now self-hosting)
- Currently assumes stateless commands. Add capability for stateful commands
- Support for exposing ports
- Support for 'hot' commands where the docker container is left running to improve performance
- Environments are clobbered every time they are persisted.  
Would be nice if it was easier to update environment from command line for aliases.

## History

March 26 2017 : First upload to github after a weekend of coding.  There will be bugs... :)

## Credits
- Thanks to the good folks at https://www.docker.com/ for creating such an awesome tool.
- Me! https://twitter.com/matt_taylor

## License

The MIT License (MIT)

Copyright 2017 Matt Taylor

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
