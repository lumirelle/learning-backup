package org.seagulls.cainiaoserver.repository;

import org.seagulls.cainiaoserver.domain.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Integer> {

}
