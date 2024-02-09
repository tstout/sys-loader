package com.github.tstout.sysloader;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import javax.sql.DataSource;

public class H2ConnFactory {

    // TODO - consider making this a pure clojure thing...
    // The compile step can be a hassle.
    public static DataSource createDS() {
        //System.out.println("------->>>>>Non-Singleton CREATE-DS Invoked");
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("sys-loader.bootstrap"));
        IFn createDsFn = Clojure.var("sys-loader.bootstrap", "create-ds");
        return DataSource.class.cast(createDsFn.invoke());
    }
}
