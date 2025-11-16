package org.seagulls.cainiaoserver.repository;

import org.seagulls.cainiaoserver.domain.ParcelStakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StakeholderRepository
    extends JpaRepository<ParcelStakeholder, Integer> {

}
