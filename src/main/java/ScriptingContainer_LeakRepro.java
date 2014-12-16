import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.util.io.ChannelDescriptor;

import java.lang.reflect.Field;
import java.util.Map;

public class ScriptingContainer_LeakRepro {
    public static void main (String[] args) {

        Map<Integer, ChannelDescriptor> descriptorMap = null;

        try {
            Field f = ChannelDescriptor.class.getDeclaredField(
                    "filenoDescriptorMap");
            f.setAccessible(true);
            descriptorMap = (Map<Integer, ChannelDescriptor>)f.get(null);
        }
        catch (Exception e)
        {
            System.err.println("Unable to get filenoDescriptorMap");
            return;
        }

        for (int i = 0; i < 10; i++) {
            ScriptingContainer sc = new ScriptingContainer
                    (LocalContextScope.SINGLETHREAD);
            sc.runScriptlet("puts 'hello world'");
            sc.terminate();
            System.out.println("Size of filenoDescriptorMap: " +
                    descriptorMap.size());

//            System.err.println("Press a key to continue");
//            try {
//                System.in.read();
//            }
//            catch (Exception e) {}
        }
    }
}
