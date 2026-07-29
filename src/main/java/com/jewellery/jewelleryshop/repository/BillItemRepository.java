package com.jewellery.jewelleryshop.repository;



import com.jewellery.jewelleryshop.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem,Long> {
}
