ScriptingContainer_LeakRepro
============================

repro case for JRuby ScriptingContainer leak of `ChannelDescriptor` objects for
SINGLETHREAD `LocalContextScope` instances.

The relevant code in this repo is [here] (./src/main/java/ScriptingContainer_LeakRepro.java).

To repro, simply:

~~~
mvn compile
mvn exec:java
~~~

This will perform the following steps 10 times:

1. Create a JRuby ScriptingContainer.
2. Run a simple Ruby scriptlet with a puts of the text 'hello world'.
3. Terminate the JRuby ScriptingContainer.
4. Output the size of the `filenoDescriptorMap` static map that the
   `ChannelDescriptor` class maintains for active descriptors.

With the leak present, each time the map size is outputted, the value increases
by 3:

~~~
hello world
Size of filenoDescriptorMap: 3
hello world
Size of filenoDescriptorMap: 6
...
~~~

Capturing stack traces of the descriptors as they are added and removed from the
`filenoDescriptorMap`, I found that the extra descriptors that remain in the map
after the end of each loop are for STDIN, STDOUT, and STDERR (https://github.com/jruby/jruby/blob/1.7.17/core/src/main/java/org/jruby/RubyGlobal.java#L211-L213).

As a hack, I had tried adding the following three lines to the `tearDown` method
in `org.jruby.Ruby`:

~~~java
((RubyIO)getGlobalVariables().get("$stdin")).close();
((RubyIO)getGlobalVariables().get("$stderr")).close();
((RubyIO)getGlobalVariables().get("$stdout")).close();
~~~

With these changes in place, the `ChannelDescriptor` objects no longer were
leaked, but the underlying STDIN, STDOUT, and STDERR streams were all closed.
This doesn't, therefore, seem to be a workable solution to the problem.




