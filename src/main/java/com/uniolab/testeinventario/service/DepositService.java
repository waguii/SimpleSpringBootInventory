package com.uniolab.testeinventario.service;

import com.uniolab.testeinventario.model.Deposit;
import com.uniolab.testeinventario.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;

    public Deposit create(Deposit deposit) {
        return depositRepository.save(deposit);
    }

}
