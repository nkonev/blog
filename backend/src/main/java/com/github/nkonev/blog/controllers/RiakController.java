package com.github.nkonev.blog.controllers;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RequestMapping("/riak")
@RestController
public class RiakController {

    @Autowired
    private RiakClient client;

    @PostMapping("/store")
    public void store(String data) throws ExecutionException, InterruptedException {
        Location location = new Location(new Namespace("TestBucket"), "TestKey");
        String myData = data;

        StoreValue sv = new StoreValue.Builder(myData).withLocation(location).build();
        StoreValue.Response svResponse = client.execute(sv);
        System.out.println(svResponse);
    }

    @GetMapping("/store")
    public String get() throws ExecutionException, InterruptedException {
        Location location = new Location(new Namespace("TestBucket"), "TestKey");

        FetchValue fv = new FetchValue.Builder(location).build();
        FetchValue.Response response = client.execute(fv);

        // Fetch object as String
        String value = response.getValue(String.class);
        System.out.println(value);
        return value;
    }
}
