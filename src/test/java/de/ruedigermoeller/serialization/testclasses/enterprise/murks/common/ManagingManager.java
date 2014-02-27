package de.ruedigermoeller.serialization.testclasses.enterprise.murks.common;

import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedDataType;
import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedInt;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 02.12.12
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */

public class ManagingManager extends Trader {
    public ManagingManager() {
    }

    public ManagingManager(ObjectOrientedInt mArtificialID, ObjectOrientedInt mUserID, ObjectOrientedDataType mBusinessIdentifier, ObjectOrientedInt mStatus, ObjectOrientedInt mHiddenBusinessUnitID, ObjectOrientedDataType mName, ObjectOrientedDataType mBusinessUnitName, ObjectOrientedInt mMarketID, ObjectOrientedInt mUserLevel, ObjectOrientedDataType mUserGroup, ObjectOrientedInt mUserCategory, ObjectOrientedDataType mLoginName, Boolean mIsUSLocated, ObjectOrientedInt mBusinessUnitID, ObjectOrientedDataType mRealName) {
        super(mArtificialID, mUserID, mBusinessIdentifier, mStatus, mHiddenBusinessUnitID, mName, mBusinessUnitName, mMarketID, mUserLevel, mUserGroup, mUserCategory, mLoginName, mIsUSLocated, mBusinessUnitID, mRealName);
    }
}
