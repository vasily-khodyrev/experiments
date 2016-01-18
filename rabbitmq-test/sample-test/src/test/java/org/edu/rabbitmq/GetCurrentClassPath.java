package org.edu.rabbitmq;

import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by
 * User: vkhodyre
 * Date: 1/14/2016
 */
public class GetCurrentClassPath {

    @Test
    public void testClassPath() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }
    }
}
