/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.desktop.main.portfolio.openoffer;

import haveno.core.offer.Offer;
import haveno.core.offer.OpenOffer;
import lombok.Getter;

/**
 * We could remove that wrapper if it is not needed for additional UI only fields.
 */
class OpenOfferListItem {
    @Getter
    private final OpenOffer openOffer;

    OpenOfferListItem(OpenOffer openOffer) {
        this.openOffer = openOffer;
    }

    OpenOfferListItem() {
        openOffer = null;
    }

    public Offer getOffer() {
        return openOffer.getOffer();
    }

    public String getGroupId() {
        return openOffer.getGroupId();
    }
}
