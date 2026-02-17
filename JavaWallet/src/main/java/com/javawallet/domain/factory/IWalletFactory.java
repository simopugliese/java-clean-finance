package com.javawallet.domain.factory;

import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;

public interface IWalletFactory {

    Wallet create(String name, WalletType type, Money initialBalance);
}