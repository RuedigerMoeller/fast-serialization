package de.ruedigermoeller.serialization.testclasses.enterprise.murks.common;

import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedDataType;
import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedInt;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

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


public class ManagingManager extends Trader {
    public ManagingManager() {
    }

    public ManagingManager(ObjectOrientedInt mArtificialID, ObjectOrientedInt mUserID, ObjectOrientedDataType mBusinessIdentifier, ObjectOrientedInt mStatus, ObjectOrientedInt mHiddenBusinessUnitID, ObjectOrientedDataType mName, ObjectOrientedDataType mBusinessUnitName, ObjectOrientedInt mMarketID, ObjectOrientedInt mUserLevel, ObjectOrientedDataType mUserGroup, ObjectOrientedInt mUserCategory, ObjectOrientedDataType mLoginName, Boolean mIsUSLocated, ObjectOrientedInt mBusinessUnitID, ObjectOrientedDataType mRealName) {
        super(mArtificialID, mUserID, mBusinessIdentifier, mStatus, mHiddenBusinessUnitID, mName, mBusinessUnitName, mMarketID, mUserLevel, mUserGroup, mUserCategory, mLoginName, mIsUSLocated, mBusinessUnitID, mRealName);
    }
}
