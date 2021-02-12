package org.example.jdbceye;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class JdbcEyeAttacher {

    public static void main(final String[] args) throws IOException, AttachNotSupportedException, URISyntaxException, AgentLoadException, AgentInitializationException {
        // Read PID if not in parameters
        String jvmPid;
        if (args.length == 0 || args[0] == null) {
            System.out.print("Java PID: ");
            Scanner inScanner = new Scanner(System.in);
            jvmPid = inScanner.nextLine();
        } else {
            jvmPid = args[0];
        }

        VirtualMachine jvm = VirtualMachine.attach(jvmPid);
        jvm.loadAgent(new File(JdbcEyeAttacher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath());
        System.out.print("Agent was successfully loaded.");
        jvm.detach();
    }

}
