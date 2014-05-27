package de.ruedigermoeller.serialization.testclasses.enterprise;


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

public class SimpleOrder implements Serializable {

    public static SimpleOrder generateOrder(int randomSeed) {
        Random rand = new Random(randomSeed);
        return new SimpleOrder(
                new Date(rand.nextLong()),
                rand.nextBoolean() ? new ObjectOrientedDataType("HipHop") : new ObjectOrientedDataType("BeBop"),
                rand.nextBoolean() ? new ObjectOrientedDataType("some special text") : new ObjectOrientedDataType("********"),
                rand.nextBoolean() ? new ObjectOrientedDataType("pok a asudh ") : new ObjectOrientedDataType("wacka wacka"),
                rand.nextBoolean() ? new ObjectOrientedDataType("killbill not yet") : new ObjectOrientedDataType("fillorkill"),
                rand.nextBoolean() ? new ObjectOrientedInt(-1) : new ObjectOrientedInt(rand.nextInt()),
                rand.nextBoolean() ? new ObjectOrientedInt(0) : new ObjectOrientedInt(rand.nextInt()),
                rand.nextBoolean() ? new ObjectOrientedInt(100) : new ObjectOrientedInt(rand.nextInt()),
                rand.nextLong(),
                rand.nextBoolean() ? new ObjectOrientedDataType("it has been reported longer may text occur here and there") : new ObjectOrientedDataType("and contain special chars äöü??")
        );
    }

    public SimpleOrder() {

    }

    public SimpleOrder(
            Date mOrderTime,
            ObjectOrientedDataType mTradableId,
            ObjectOrientedDataType mTraderId,
            ObjectOrientedDataType mBuySell,
            ObjectOrientedDataType mOrderType,
            ObjectOrientedInt mOrderQty,
            ObjectOrientedInt mInitialOrderQty,
            ObjectOrientedInt mOrderPrc,
            long mOrderId,
            ObjectOrientedDataType mText
    ) {
        setOrderTime(mOrderTime);
        setTradableId(mTradableId);
        setTraderId(mTraderId);
        setBuySell(mBuySell);
        setOrderType(mOrderType);
        setOrderQty(mOrderQty);
        setInitialOrderQty(mInitialOrderQty);
        setOrderPrc(mOrderPrc);
        setOrderId(mOrderId);
        setText(mText);
    }

    Date mOrderTime;
    ObjectOrientedDataType mTradableId;
    ObjectOrientedDataType mTraderId;
    ObjectOrientedDataType mBuySell;
    ObjectOrientedDataType mOrderType;
    ObjectOrientedInt mOrderQty;
    ObjectOrientedInt mInitialOrderQty;
    ObjectOrientedInt mOrderPrc;
    Long mOrderId;
    ObjectOrientedDataType mText;

    public Date getOrderTime() {
        return mOrderTime;
    }

    public void setOrderTime(Date aOrderTime) {
        mOrderTime = aOrderTime;
    }

    public ObjectOrientedDataType getTradableId() {
        return mTradableId;
    }

    public void setTradableId(ObjectOrientedDataType aTradableId) {
        mTradableId = aTradableId;
    }

    public ObjectOrientedDataType getTraderId() {
        return mTraderId;
    }

    public void setTraderId(ObjectOrientedDataType aTraderId) {
        mTraderId = aTraderId;
    }

    public ObjectOrientedDataType getBuySell() {
        return mBuySell;
    }

    public void setBuySell(ObjectOrientedDataType aBuySell) {
        mBuySell = aBuySell;
    }

    public ObjectOrientedDataType getOrderType() {
        return mOrderType;
    }

    public void setOrderType(ObjectOrientedDataType aOrderType) {
        mOrderType = aOrderType;
    }

    public ObjectOrientedInt getOrderQty() {
        return mOrderQty;
    }

    public void setOrderQty(ObjectOrientedInt aOrderQty) {
        mOrderQty = aOrderQty;
    }

    public ObjectOrientedInt getInitialOrderQty() {
        return mInitialOrderQty;
    }

    public void setInitialOrderQty(ObjectOrientedInt aInitialOrderQty) {
        mInitialOrderQty = aInitialOrderQty;
    }

    public ObjectOrientedInt getOrderPrc() {
        return mOrderPrc;
    }

    public void setOrderPrc(ObjectOrientedInt aOrderPrc) {
        mOrderPrc = aOrderPrc;
    }

    public long getOrderId() {
        return mOrderId;
    }

    public void setOrderId(long aOrderId) {
        mOrderId = aOrderId;
    }

    public ObjectOrientedDataType getText() {
        return mText;
    }

    public void setText(ObjectOrientedDataType aText) {
        mText = aText;
    }

    public int hashCode() {
        return getOrderTime().hashCode()^getTraderId().hashCode()^(int)getOrderId();
    }

    public boolean equals( Object o ) {
        if ( o instanceof SimpleOrder == false) {
            return false;
        }
        SimpleOrder so = (SimpleOrder) o;
        return
                so.getOrderTime().equals(getOrderTime()) &&
                so.getTradableId().equals(getTradableId()) &&
                        so.getTraderId().equals(getTraderId()) &&
                        so.getBuySell().equals(getBuySell()) &&
                        so.getOrderType().equals(getOrderType()) &&
                        so.getOrderQty().equals(getOrderQty()) &&
                        so.getInitialOrderQty().equals(getInitialOrderQty()) &&
                        so.getOrderPrc().equals(getOrderPrc()) &&
                        so.getText().equals(getText()) &&
                        so.getOrderId() == getOrderId();
    }

    public String toString() {
        return "" +
                "OrderTime = " + getOrderTime() + "\n" +
                "TradableId = " + getTradableId() + "\n" +
                "TraderId = " + getTraderId() + "\n" +
                "BuySell = " + getBuySell() + "\n" +
                "OrderType = " + getOrderType() + "\n" +
                "OrderQty = " + getOrderQty() + "\n" +
                "InitialOrderQty = " + getInitialOrderQty() + "\n" +
                "OrderPrc = " + getOrderPrc() + "\n" +
                "OrderId = " + getOrderId() + "\n" +
                "Text = " + getText() + "\n" +
                "";
    }

}
