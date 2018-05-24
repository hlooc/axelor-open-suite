/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceImpl implements OpportunitySaleOrderService  {

	@Inject
	protected SaleOrderCreateService saleOrderCreateService;

	@Inject
	protected SaleOrderRepository saleOrderRepo;

	@Inject
	protected AppBaseService appBaseService;


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException  {
		Currency currency;
		if (opportunity.getCurrency() != null) {
			currency = opportunity.getCurrency();
		} else if (opportunity.getPartner() != null && opportunity.getPartner().getCurrency() != null) {
			currency = opportunity.getPartner().getCurrency();
		} else {
			currency = opportunity.getCompany().getCurrency();
		}

		SaleOrder saleOrder = createSaleOrder(opportunity, currency);

		opportunity.addSaleOrderListItem(saleOrder);

        if (opportunity.getSalesStageSelect() < OpportunityRepository.SALES_STAGE_PROPOSITION) {
            opportunity.setSalesStageSelect(OpportunityRepository.SALES_STAGE_PROPOSITION);
        }

		saleOrderRepo.save(saleOrder);

		return saleOrder;
	}

	protected SaleOrder createSaleOrder(Opportunity opportunity, Currency currency) throws AxelorException {
		return saleOrderCreateService.createSaleOrder(opportunity.getUser(), opportunity.getCompany(), null, currency, null, opportunity.getName(), null,
				appBaseService.getTodayDate(), Beans.get(PartnerPriceListService.class).getDefaultPriceList(opportunity.getPartner(), PriceListRepository.TYPE_SALE), opportunity.getPartner(), opportunity.getTeam());
	}

}
