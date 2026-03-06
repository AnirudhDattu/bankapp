package com.bank.izbank.database;

import android.app.Application;

import com.parse.Parse;

public class ParseStarterClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("kyxZ6EoiwV9TqrXGuguBt5CMrDgyXHgioqi4F2R0")
                // if desired
                .clientKey("o2vArAj1HQAhYKde94aC0wGy4AzJRQEAp9XhpiPl")
                .server("https://parseapi.back4app.com/")
                .build()
        );

    }
}
