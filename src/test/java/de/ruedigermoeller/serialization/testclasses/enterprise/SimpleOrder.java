package de.ruedigermoeller.serialization.testclasses.enterprise;

import de.ruedigermoeller.serialization.annotations.EqualnessIsBinary;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: moelrue
 * Date: 22.11.12
 * Time: 19:07
 * To change this template use File | Settings | File Templates.
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
