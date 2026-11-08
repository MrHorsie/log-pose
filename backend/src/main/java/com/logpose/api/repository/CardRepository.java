package com.logpose.api.repository;

import com.logpose.api.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Optional<Card> findByCardNumberIgnoreCase(String cardNumber);
}
