package com.github.tstout.sysloader;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import javax.sql.DataSource;

public class H2ConnFactory {
    // enum Singleton {
    //     INSTANCE;

    //     IFn createDsFn;

    //     private Singleton() {
            // IFn require = Clojure.var("clojure.core", "require");
            // require.invoke(Clojure.read("sys-loader.core"));
            // createDsFn = Clojure.var("sys-loader.core", "create-ds");
    //     }
    // }

    // TODO - make this a memozied clojure fn and get rid of the 
    // static singleton
    public static DataSource createDS() {
        System.out.println("------->>>>>Non-Singleton CREATE-DS Invoked");
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("sys-loader.core"));
        IFn createDsFn = Clojure.var("sys-loader.core", "create-ds");
        return DataSource.class.cast(createDsFn.invoke());
        
        //return DataSource.class.cast(Singleton.INSTANCE.createDsFn.invoke());
    }
}
