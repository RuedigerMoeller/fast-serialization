package de.ruedigermoeller.serialization.testclasses.enterprise;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.*;
import java.util.*;

/**
 Copyright [2014] Ruediger Moeller

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */


public class Trader implements Serializable, HasDescription {

    public static Trader generateTrader(int randomSeed, boolean fillTrArray) {
        Random rand = new Random(randomSeed);
        Trader tr = new Trader(
                rand.nextBoolean() ? new ObjectOrientedInt(rand.nextInt()) : new ObjectOrientedInt(0),
                rand.nextBoolean() ? new ObjectOrientedInt(rand.nextInt(10000)) : new ObjectOrientedInt(-1),
                rand.nextBoolean() ? new ObjectOrientedDataType("Jim "+rand.nextInt(10000)) : new ObjectOrientedDataType("Bob "+rand.nextInt(100)),
                null,
                null, //mHiddenBusinessUnitID
                rand.nextBoolean() ? new ObjectOrientedDataType(""+rand.nextInt()) : new ObjectOrientedDataType("Bob "+rand.nextInt()),
                new ObjectOrientedDataType("lucky luke"+rand.nextInt(788)),
                new ObjectOrientedInt(rand.nextInt(200)),
                new ObjectOrientedInt(rand.nextInt(4)),
                new ObjectOrientedDataType("bill jolly"+rand.nextInt()),
                new ObjectOrientedInt(rand.nextInt()),
                new ObjectOrientedDataType("Jonny not Jim Bob"+rand.nextInt()),
                rand.nextBoolean(),
                new ObjectOrientedInt(rand.nextInt()),
                new ObjectOrientedDataType("pokpok aber hallo"+rand.nextInt())
        );
        int orders = 10;
        for ( int i = 0; i < orders; i++ ) {
            tr.orders.add(SimpleOrder.generateOrder(i+randomSeed));
        }
        if ( fillTrArray ) {
            int trs = 5;
            for ( int i = 0; i < trs; i++ ) {
                Trader friend = Trader.generateTrader(i + randomSeed, false);
                tr.friends.add(friend);
                int start = Math.max(0, rand.nextInt(orders-5));
                int len = rand.nextInt(5);
                for (int j = start; j < start+len; j++ ) {
                    tr.ordersOfFriends.put(tr.getLoginName().toString(),SimpleOrder.generateOrder(j+randomSeed));
                }
            }
        }
        return tr;
    }

    public Trader() { }

    public Trader(
            ObjectOrientedInt mArtificialID,
            ObjectOrientedInt mUserID,
            ObjectOrientedDataType mBusinessIdentifier,
            ObjectOrientedInt mStatus,
            ObjectOrientedInt mHiddenBusinessUnitID,
            ObjectOrientedDataType mName,
            ObjectOrientedDataType mBusinessUnitName,
            ObjectOrientedInt mMarketID,
            ObjectOrientedInt mUserLevel,
            ObjectOrientedDataType mUserGroup,
            ObjectOrientedInt mUserCategory,
            ObjectOrientedDataType mLoginName,
            Boolean mIsUSLocated,
            ObjectOrientedInt mBusinessUnitID,
            ObjectOrientedDataType mRealName
    )
    {
        this();
        setArtificialID(mArtificialID);
        setUserID(mUserID);
        setBusinessIdentifier(mBusinessIdentifier);
        setStatus(mStatus);
        setHiddenBusinessUnitID(mHiddenBusinessUnitID);
        setName(mName);
        setBusinessUnitName(mBusinessUnitName);
        setMarketID(mMarketID);
        setUserLevel(mUserLevel);
        setUserGroup(mUserGroup);
        setUserCategory(mUserCategory);
        setLoginName(mLoginName);
        setIsUSLocated(mIsUSLocated);
        setBusinessUnitID(mBusinessUnitID);
        setRealName(mRealName);
    }

    ObjectOrientedInt mArtificialID;
    ObjectOrientedInt mUserID;
    ObjectOrientedDataType mBusinessIdentifier;
    ObjectOrientedInt mStatus;
    ObjectOrientedInt mHiddenBusinessUnitID;
    ObjectOrientedDataType mName;
    ObjectOrientedDataType mBusinessUnitName;
    ObjectOrientedInt mMarketID;
    ObjectOrientedInt mUserLevel;
    ObjectOrientedDataType mUserGroup;
    ObjectOrientedInt mUserCategory;
    ObjectOrientedDataType mLoginName;
    Boolean mIsUSLocated;
    ObjectOrientedInt mBusinessUnitID;
    ObjectOrientedDataType mRealName;

    List<SimpleOrder> orders = new ArrayList<SimpleOrder>();
    List<Trader> friends = new ArrayList<Trader>();
    // modified as jackson cannot stand non string keys
    HashMap<String/*TraderName*/,SimpleOrder> ordersOfFriends = new HashMap<>();

    public ObjectOrientedInt getArtificialID() {
        return mArtificialID;
    }
    public void setArtificialID( ObjectOrientedInt aArtificialID) {
        mArtificialID = aArtificialID;
    }

    public ObjectOrientedInt getUserID() {
        return mUserID;
    }
    public void setUserID( ObjectOrientedInt aUserID) {
        mUserID = aUserID;
    }

    public ObjectOrientedDataType getBusinessIdentifier() {
        return mBusinessIdentifier;
    }
    public void setBusinessIdentifier( ObjectOrientedDataType aBusinessIdentifier) {
        mBusinessIdentifier = aBusinessIdentifier;
    }

    public ObjectOrientedInt getStatus() {
        return mStatus;
    }
    public void setStatus( ObjectOrientedInt aStatus) {
        mStatus = aStatus;
    }

    public ObjectOrientedInt getHiddenBusinessUnitID() {
        return mHiddenBusinessUnitID;
    }
    public void setHiddenBusinessUnitID( ObjectOrientedInt aHiddenBusinessUnitID) {
        mHiddenBusinessUnitID = aHiddenBusinessUnitID;
    }

    public ObjectOrientedDataType getName() {
        return mName;
    }
    public void setName( ObjectOrientedDataType aName) {
        mName = aName;
    }

    public ObjectOrientedDataType getBusinessUnitName() {
        return mBusinessUnitName;
    }
    public void setBusinessUnitName( ObjectOrientedDataType aBusinessUnitName) {
        mBusinessUnitName = aBusinessUnitName;
    }

    public ObjectOrientedInt getMarketID() {
        return mMarketID;
    }
    public void setMarketID( ObjectOrientedInt aMarketID) {
        mMarketID = aMarketID;
    }

    public ObjectOrientedInt getUserLevel() {
        return mUserLevel;
    }
    public void setUserLevel( ObjectOrientedInt aUserLevel) {
        mUserLevel = aUserLevel;
    }

    public ObjectOrientedDataType getUserGroup() {
        return mUserGroup;
    }
    public void setUserGroup( ObjectOrientedDataType aUserGroup) {
        mUserGroup = aUserGroup;
    }

    public ObjectOrientedInt getUserCategory() {
        return mUserCategory;
    }
    public void setUserCategory( ObjectOrientedInt aUserCategory) {
        mUserCategory = aUserCategory;
    }

    public ObjectOrientedDataType getLoginName() {
        return mLoginName;
    }
    public void setLoginName( ObjectOrientedDataType aLoginName) {
        mLoginName = aLoginName;
    }

    public Boolean getIsUSLocated() {
        return mIsUSLocated;
    }
    public void setIsUSLocated( Boolean aIsUSLocated) {
        mIsUSLocated = aIsUSLocated;
    }

    public ObjectOrientedInt getBusinessUnitID() {
        return mBusinessUnitID;
    }
    public void setBusinessUnitID( ObjectOrientedInt aBusinessUnitID) {
        mBusinessUnitID = aBusinessUnitID;
    }

    public ObjectOrientedDataType getRealName() {
        return mRealName;
    }
    public void setRealName( ObjectOrientedDataType aRealName) {
        mRealName = aRealName;
    }

    public String toString() {
        return ""+
                "ArtificialID = "+ getArtificialID()+"\n"+
                "UserID = "+ getUserID()+"\n"+
                "BusinessIdentifier = "+ getBusinessIdentifier()+"\n"+
                "Status = "+ getStatus()+"\n"+
                "HiddenBusinessUnitID = "+ getHiddenBusinessUnitID()+"\n"+
                "Name = "+ getName()+"\n"+
                "BusinessUnitName = "+ getBusinessUnitName()+"\n"+
                "MarketID = "+ getMarketID()+"\n"+
                "UserLevel = "+ getUserLevel()+"\n"+
                "UserGroup = "+ getUserGroup()+"\n"+
                "UserCategory = "+ getUserCategory()+"\n"+
                "LoginName = "+ getLoginName()+"\n"+
                "IsUSLocated = "+ getIsUSLocated()+"\n"+
                "BusinessUnitID = "+ getBusinessUnitID()+"\n"+
                "RealName = "+ getRealName()+"\n"+
                "";
    }

    @Override
    public String getDescription() {
        return "Measures serialization of a typical 'Enterprise-Object' using Value-Classes instead of primitive tpyes.";
    }
}
