package com.jewellery.jewelleryshop.repository;


import com.jewellery.jewelleryshop.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {


    Optional<Admin> findByUserName(String userName);

    Optional<Admin> findByEmail(String email);


}
