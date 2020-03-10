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

package com.privateinternetaccess.android.pia.model.response;

import com.privateinternetaccess.android.pia.model.Invite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InvitesResponse {

    private int numberInvites;
    private int numberRewarded;
    private int freeDays;

    private String referralLink;

    private Invite[] invitesArray;

    private List<Invite> sentInvites;
    private List<Invite> signupInvites;

    private Exception exception;

    public int getNumberInvites() {
        return numberInvites;
    }

    public int getNumberRewarded() {
        return numberRewarded;
    }

    public int getFreeDays() {
        return freeDays;
    }

    public String getReferralLink() {
        return referralLink;
    }

    public List<Invite> getSentInvites() {
        return sentInvites;
    }

    public List<Invite> getSignupInvites() {
        return signupInvites;
    }

    public void setNumberInvites(int numberInvites) {
        this.numberInvites = numberInvites;
    }

    public void setNumberRewarded(int numberRewarded) {
        this.numberRewarded = numberRewarded;
    }

    public void setFreeDays(int freeDays) {
        this.freeDays = freeDays;
    }

    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void parse(JSONObject object){
        try {
            setNumberInvites(object.optInt("total_invites_sent"));
            setNumberRewarded(object.optInt("total_invites_rewarded"));
            setFreeDays(object.optInt("total_free_days_given"));
            setReferralLink(object.optString("unique_referral_link"));

            JSONArray invites = object.optJSONArray("invites");

            if (invites != null) {
                invitesArray = new Invite[invites.length()];

                for (int i = 0; i < invites.length(); i++) {
                    JSONObject invite = invites.getJSONObject(i);
                    Invite inviteObj = new Invite();

                    inviteObj.rewarded = invite.optBoolean("rewarded");
                    inviteObj.accepted = invite.optBoolean("accepted");
                    inviteObj.obfuscatedEmail = invite.optString("obfuscated_email");
                    inviteObj.gracePeriod = invite.optString("grace_period_remaining");

                    invitesArray[i] = inviteObj;
                }
            }
            else {
                invitesArray = new Invite[0];
            }

            splitInvites();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitInvites() {
        sentInvites = new ArrayList<>();
        signupInvites = new ArrayList<>();

        for (Invite invite : invitesArray) {
            if (invite.accepted) {
                signupInvites.add(invite);
            }
            else {
                sentInvites.add(invite);
            }
        }
    }
}
