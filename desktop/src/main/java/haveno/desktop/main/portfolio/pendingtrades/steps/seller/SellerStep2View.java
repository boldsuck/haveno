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

package haveno.desktop.main.portfolio.pendingtrades.steps.seller;

import haveno.core.locale.Res;
import haveno.core.payment.payload.F2FAccountPayload;
import haveno.desktop.components.paymentmethods.F2FForm;
import haveno.desktop.main.portfolio.pendingtrades.PendingTradesViewModel;
import haveno.desktop.main.portfolio.pendingtrades.steps.TradeStepView;
import haveno.desktop.util.Layout;

import static com.google.common.base.Preconditions.checkNotNull;
import static haveno.desktop.util.FormBuilder.addTitledGroupBg;

public class SellerStep2View extends TradeStepView {

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, Initialisation
    ///////////////////////////////////////////////////////////////////////////////////////////

    public SellerStep2View(PendingTradesViewModel model) {
        super(model);
    }

    @Override
    protected void addContent() {
        addTradeInfoBlock();
        addInfoBlock();
        checkNotNull(model.dataModel.getTrade(), "No trade found");
        checkNotNull(model.dataModel.getTrade().getOffer(), "No offer found");
        if (model.dataModel.getSellersPaymentAccountPayload() instanceof F2FAccountPayload) {
            addTitledGroupBg(gridPane, ++gridRow, 4,
                    Res.get("portfolio.pending.step2_seller.f2fInfo.headline"), Layout.COMPACT_GROUP_DISTANCE);
            gridRow = F2FForm.addStep2Form(gridPane, --gridRow, model.dataModel.getSellersPaymentAccountPayload(),
                    model.dataModel.getTrade().getOffer(), Layout.COMPACT_FIRST_ROW_AND_GROUP_DISTANCE, false);
        }
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void onPendingTradesInitialized() {
        super.onPendingTradesInitialized();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Info
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getInfoBlockTitle() {
        return Res.get("portfolio.pending.step2_seller.waitPayment.headline");
    }

    @Override
    protected String getInfoText() {
        return Res.get("portfolio.pending.step2_seller.waitPayment.msg", getCurrencyCode(trade));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Warning
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getFirstHalfOverWarnText() {
        return Res.get("portfolio.pending.step2_seller.warn",
                getCurrencyCode(trade),
                model.getDateForOpenDispute());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Dispute
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getPeriodOverWarnText() {
        return Res.get("portfolio.pending.step2_seller.openForDispute");
    }

    @Override
    protected void applyOnDisputeOpened() {
    }
}


