package com.mippay.repository.Client;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.Client;

import java.util.Map;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

	// Find client by user ID
	Optional<Client> findByUserId(String userId);

	// Find client by email (added for edit-profile)
	Optional<Client> findByEmail(String email);

	// Check if email exists excluding current client
	boolean existsByEmailAndUserIdNot(String email, String userId);

	// Check if mobile number exists excluding current client
	boolean existsByMobileNumAndUserIdNot(String mobileNum, String userId);

	@Query(value = "select account_bal from clients where user_id =:userId", nativeQuery = true)
	double getWalletBalance(String userId);

	@Transactional
	@Modifying
	@Query(value = "update clients set account_bal =:v where user_id =:userId", nativeQuery = true)
	void updateBalance(String userId, double v);

	@Transactional
	@Modifying
	@Query(value = "update clients set account_bal = account_bal - :v where user_id =:userId and account_bal >= :v", nativeQuery = true)
	int updateBalance1(String userId, double v);

	@Transactional
	@Modifying
	@Query(value = "update clients set status =:status where user_id =:clientId", nativeQuery = true)
	void updateStatus(String status, String clientId);

	@Transactional
	@Modifying
	@Query(value = "Delete from clients where user_id =:clientId", nativeQuery = true)
	void deleteByClientId(String clientId);

	@Transactional
	@Modifying
	@Query(value = "update clients set account_bal = account_bal + :newBal where user_id =:userId", nativeQuery = true)
	int updateWallet(double newBal, String userId);

	@Transactional
	@Modifying
	@Query(value = "update clients set otp=:otp where email=:email", nativeQuery = true)
	void saveOtpInClient(String s, String email);

	@Transactional
	@Modifying
	@Query(value = "update clients set password=:encPass where email=:email", nativeQuery = true)
	void updatePassword(String encPass, String email);

	@Transactional
	@Modifying
	@Query(value = "update clients set email =:email,password=:password, name =:name, mobile_num =:mobileNum where user_id=:userId", nativeQuery = true)
	void updateClient(String email, String password, String name, String mobileNum, String userId);

	@Modifying
	@Transactional
	@Query("UPDATE Client c SET c.walletBalance = :balance WHERE c.userId = :userId")
	int updateWalletBalance(@Param("balance") Double balance, @Param("userId") String userId);

	// LIST PAYIN MERCHANTS

	@Query(
		    value = """
		        SELECT
		            user_id                       AS merchantId,
		            name                          AS merchantName,
		            bank_name                    AS bankName,
		            CONCAT('XXXX', RIGHT(account_num, 4)) AS accountNo,
		            status                        AS status,
		            created_date                  AS createdDate
		        FROM clients
		        WHERE merchant_type = 'PAYIN'
		          AND (
		                :search IS NULL
		                OR user_id LIKE :search
		                OR name LIKE :search
		              )
		        """,
		    countQuery = """
		        SELECT COUNT(*)
		        FROM clients
		        WHERE merchant_type = 'PAYIN'
		          AND (
		                :search IS NULL
		                OR user_id LIKE :search
		                OR name LIKE :search
		              )
		        """,
		    nativeQuery = true
		)
		Page<Map<String, Object>> findAllPayinMerchants(
		        @Param("search") String search,
		        Pageable pageable
		);


	// COUNT FOR PAGINATION

	@Query(value = """
			SELECT COUNT(*)
			FROM clients
			WHERE merchant_type = 'PAYIN'
			  AND (:search IS NULL
			       OR user_id LIKE :search
			       OR name LIKE :search)
			""", nativeQuery = true)
	long countPayinMerchants(@Param("search") String search);

	// MERCHANT DETAILS BY ID
	@Query(value = """
			SELECT
			    user_id        AS merchantId,
			    name           AS merchantName,
			    name           AS legalName,
			    status         AS status,
			    created_date   AS registrationDate,

			    email          AS email,
			    mobile_num     AS phone,
			    address        AS address,

			    bank_name      AS bankName,
			    CONCAT('XXXX', RIGHT(account_num, 4)) AS accountNumber,
			    ifsc_code      AS ifscCode,

			    gst            AS gstNumber,
			    pan            AS panNumber
			FROM clients
			WHERE user_id = :merchantId
			  AND merchant_type = 'PAYIN'
			""", nativeQuery = true)
	Map<String, Object> findMerchantDetailsById(@Param("merchantId") String merchantId);

	// LIST PAYOUT MERCHANTS
	@Query(value = """
			SELECT
			    user_id              AS merchantId,
			    name                 AS merchantName,
			    bank_name            AS bankName,
			    CONCAT('XXXX', RIGHT(account_num, 4)) AS accountNo,
			    status               AS status,
			    created_date         AS createdDate
			FROM clients
			WHERE merchant_type = 'PAYOUT'
			  AND (:search IS NULL
			       OR user_id LIKE :search
			       OR name LIKE :search)
			""", countQuery = """
			SELECT COUNT(*)
			FROM clients
			WHERE merchant_type = 'PAYOUT'
			  AND (:search IS NULL
			       OR user_id LIKE :search
			       OR name LIKE :search)
			""", nativeQuery = true)
	Page<Map<String, Object>> findAllPayoutMerchants(@Param("search") String search, Pageable pageable);

//COUNT PAYOUT MERCHANTS
	@Query(value = """
			SELECT COUNT(*)
			FROM clients
			WHERE merchant_type = 'PAYOUT'
			  AND (:search IS NULL
			       OR user_id LIKE :search
			       OR name LIKE :search)
			""", nativeQuery = true)
	long countPayoutMerchants(@Param("search") String search);

	// PAYOUT MERCHANT DETAILS
	@Query(value = """
			SELECT
			    user_id        AS merchantId,
			    name           AS merchantName,
			    name           AS legalName,
			    status         AS status,
			    created_date   AS registrationDate,

			    email          AS email,
			    mobile_num     AS phone,
			    address        AS address,

			    bank_name      AS bankName,
			    CONCAT('XXXX', RIGHT(account_num, 4)) AS accountNumber,
			    ifsc_code      AS ifscCode,

			    gst            AS gstNumber,
			    pan            AS panNumber
			FROM clients
			WHERE user_id = :merchantId
			  AND merchant_type = 'PAYOUT'
			""", nativeQuery = true)
	Map<String, Object> findPayoutMerchantDetailsById(@Param("merchantId") String merchantId);
	
	
	 @Query(
		        value = "SELECT name FROM clients WHERE user_id = :userId",
		        nativeQuery = true
		    )
		    String findClientNameByUserId(@Param("userId") String userId);
}
