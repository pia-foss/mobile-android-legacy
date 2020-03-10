/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.pia.handler;

import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.model.PIAServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class PiaServerComparatorsTest {

    List<PIAServer> servers;
    List<PIAServer> serversWithTesting;
    Map<String, Long> pings;
    Map<String, Long> pingsWithTesting;
    HashSet<String> favorites;

    final String[] serverNames = new String[]{"US San Francisco","JP Tokyo", "UK London",
            "AU Sydney", "DE Berlin",
            "US New York", "CA Toronto",
            "WK Birnin Zana", "US Gotham City", "AZ Azeroth",
            "US South Park",  "HT HalloweenTown", "HP Godric Hallows",
            "SK Winterfell", "FC Braavos", "FC Hardhome"
            };

    final long[] pingsDefault = new long[]{62L, 138L, 99L,
            210L, 170L,
            34L, 40L,
            52L, 608L, 120L,
            108L, 1013L, 640L,
            999L, 999L, 999L};

    final String[] favs = new String[]{"US New York","CA Toronto","AU Sydney","JP Tokyo","DE Berlin"};

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        initLists();
    }

    public void initLists(){
        servers = new ArrayList<>();
        pings = new HashMap<>();
        favorites = new HashSet<>();
        serversWithTesting = new ArrayList<>();
        pingsWithTesting = new HashMap<>();

        for(int i = 0; i < serverNames.length; i++){
            String s = serverNames[i];
            PIAServer server = new PIAServer();
            String key = nameToKey(s);
            server.setKey(key);
            server.setName(s);
            servers.add(server);

            pings.put(key, pingsDefault[i]);
        }

        favorites.addAll(Arrays.asList(favs));
        serversWithTesting.addAll(servers);
        pingsWithTesting.putAll(pings);
        initTestServers();
    }

    public void initTestServers(){
        PIAServer test1 = new PIAServer();
        test1.setName("US Ceres Station");
        String key = nameToKey(test1.getName());
        test1.setKey(key);
        test1.setTesting(true);
        serversWithTesting.add(test1);

        pingsWithTesting.put(key, 476L);

        PIAServer test2 = new PIAServer();
        test2.setName("VS Fortuna");
        String key2 = nameToKey(test2.getName());
        test2.setKey(key2);
        test2.setTesting(true);
        serversWithTesting.add(test2);

        pingsWithTesting.put(key2, 240L);
    }


    private String nameToKey(String name){
        return name.replace(" ", "_").toLowerCase();
    }

    //creation test fails due to needing context

    @Test
    public void sortingPingTest_firstPosition(){
        List<PIAServer> sortedByPing = new ArrayList<>(servers);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pings));
        PIAServer first = sortedByPing.get(0);
        Assert.assertTrue(first.getKey().equals("us_new_york"));
    }

    @Test
    public void sortingPingTest_fourthPosition(){
        List<PIAServer> sortedByPing = new ArrayList<>(servers);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pings));
        PIAServer fourth = sortedByPing.get(3);
        Assert.assertTrue(fourth.getKey().equals("us_san_francisco"));
    }

    @Test
    public void sortingPingTest_lastPosition(){
        List<PIAServer> sortedByPing = new ArrayList<>(servers);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pings));
        PIAServer last = sortedByPing.get(sortedByPing.size()-1);
        Assert.assertTrue(last.getKey().equals("ht_halloweentown"));
    }

    @Test
    public void sortingNameTest_firstPosition(){
        List<PIAServer> sortedByName = new ArrayList<>(servers);
        Collections.sort(sortedByName, new PIAServerHandler.ServerNameComperator());
        PIAServer first = sortedByName.get(0);
        Assert.assertTrue(first.getKey().equals("au_sydney"));
    }

    @Test
    public void sortingNameTest_fourthPosition(){
        List<PIAServer> sortedByName = new ArrayList<>(servers);
        Collections.sort(sortedByName, new PIAServerHandler.ServerNameComperator());
        PIAServer first = sortedByName.get(3);
        Assert.assertTrue(first.getKey().equals("de_berlin"));
    }

    @Test
    public void sortingNameTest_lastPosition(){
        List<PIAServer> sortedByName = new ArrayList<>(servers);
        Collections.sort(sortedByName, new PIAServerHandler.ServerNameComperator());
        PIAServer first = sortedByName.get(sortedByName.size()-1);
        Assert.assertTrue(first.getKey().equals("wk_birnin_zana"));
    }

    @Test
    public void sortingFavoritesTest_firstPosition(){
        List<PIAServer> sortedByFavorites = new ArrayList<>(servers);
        Collections.sort(sortedByFavorites, new PIAServerHandler.ServerNameComperator());
        Collections.sort(sortedByFavorites, new PIAServerHandler.FavoriteComperator(favorites));
        PIAServer first = sortedByFavorites.get(0);
        Assert.assertTrue(first.getKey().equals("au_sydney"));
    }

    @Test
    public void sortingFavoritesTest_fifthPosition(){
        List<PIAServer> sortedByFavorites = new ArrayList<>(servers);
        Collections.sort(sortedByFavorites, new PIAServerHandler.ServerNameComperator());
        Collections.sort(sortedByFavorites, new PIAServerHandler.FavoriteComperator(favorites));
        PIAServer first = sortedByFavorites.get(favorites.size()-1);
        Assert.assertTrue(first.getKey().equals("us_new_york"));
    }
    @Test
    public void sortingFavoritesTest_firstPosition_nonFavorite(){
        List<PIAServer> sortedByFavorites = new ArrayList<>(servers);
        Collections.sort(sortedByFavorites, new PIAServerHandler.ServerNameComperator());
        Collections.sort(sortedByFavorites, new PIAServerHandler.FavoriteComperator(favorites));
        PIAServer first = sortedByFavorites.get(favorites.size());
        Assert.assertTrue(first.getKey().equals("az_azeroth"));
    }

    //Add test servers to make sure the tests servers are in it. You can't confirm order

    @Test
    public void sortingPingTest_withTestingServers_first(){
        List<PIAServer> sortedByPing = new ArrayList<>(serversWithTesting);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pingsWithTesting));
        PIAServer first = sortedByPing.get(0);
        Assert.assertTrue(first.getKey().equals("us_ceres_station"));
    }

    @Test
    public void sortingPingTest_withTestingServers_second(){
        List<PIAServer> sortedByPing = new ArrayList<>(serversWithTesting);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pingsWithTesting));
        PIAServer first = sortedByPing.get(1);
        Assert.assertTrue(first.getKey().equals("vs_fortuna"));
    }

    @Test
    public void sortingPingTest_withTestingServers_firstNonTesting(){
        List<PIAServer> sortedByPing = new ArrayList<>(serversWithTesting);
        Collections.sort(sortedByPing, new PIAServerHandler.PingComperator(pingsWithTesting));
        PIAServer first = sortedByPing.get(2);
        Assert.assertTrue(first.getKey().equals("us_new_york"));
    }

    @Test
    public void sortingNameTest_withTestingServers_first(){
        List<PIAServer> sortedByName = new ArrayList<>(serversWithTesting);
        Collections.sort(sortedByName, new PIAServerHandler.ServerNameComperator());
        PIAServer first = sortedByName.get(0);
        Assert.assertTrue(first.getKey().equals("us_ceres_station"));
    }

    @Test
    public void sortingNameTest_withTestingServers_firstNonTesting(){
        List<PIAServer> sortedByName = new ArrayList<>(serversWithTesting);
        Collections.sort(sortedByName, new PIAServerHandler.ServerNameComperator());
        PIAServer first = sortedByName.get(2);
        Assert.assertTrue(first.getKey().equals("au_sydney"));
    }

    @After
    public void close(){
        servers = null;
        serversWithTesting = null;
        pings = null;
        pingsWithTesting = null;
        favorites = null;
    }
}
