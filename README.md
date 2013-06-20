Lurker is a tiny common [java agent][0], it is incubate from [HouseMD][1].


[0]:https://www.google.com/search?q=java+agent
[1]:https://github.com/csug/housemd

## Usage

### Run as javaagent

    > java -javaagent:lurker.jar=<url> ...


### Run for attaching

    > java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar lurker.jar <pid> <url>

> Tips: In Mac OSX and using JDK6, `-Xbootclasspath/a:$JAVA_HOME/lib/tools.jar ` should be ignored.


### URL

The `url` should provide three informations:

1. The `classpath` which can load the `real agent class`;
2. The `real agent class` name;
3. The optional configurations.

A example of `url` is:

    http://hostname/classpath?bootstrap=real.XXXAgent&key=value

`curl` the `url`,  a text content of classpath should be return, it could tell lurker where to load the classes.

`real.XXXAgent` is the `real agent class` , which lurker should bootstrap first.

Then, `key=value` would be configuration for instancing `real.XXXAgent`.

## Real Agent Class

Every `real agent class` should have:

1. A public constructor;
2. A public void method named `apply` without any arguments.

The public constructor may have one or two arguments, like:

```
package real;

public class SimpleAgent {

    public SimpleAgent() { ... }

    public void apply() { ... }

}

public class ConfigurableAgent {
    public ConfigurableAgent(Map<String, String> conf) { ... }

    public void apply() { ... }

}


public class InstrumentationAgent {
    public InstrumentationAgent(Instrumentation inst) { ... }

    public void apply() { ... }

}

public class SuperAgent {
    public SuperAgent(Map<String, String> conf, Instrumentation inst) { ... }

    public void apply() { ... }

}
```

## Copyright and license

Copyright 2013 zhongl

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
