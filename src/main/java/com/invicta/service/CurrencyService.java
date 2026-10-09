package com.invicta.service;

import com.invicta.model.SystemModels;

import java.util.List;

public interface CurrencyService {
    List<SystemModels.Currency> list(Integer chainId);
}
