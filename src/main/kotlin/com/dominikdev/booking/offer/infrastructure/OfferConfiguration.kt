package com.dominikdev.booking.offer.infrastructure

import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.offer.DefaultOfferFacade
import com.dominikdev.booking.offer.OfferFacade
import com.dominikdev.booking.offer.application.*
import com.dominikdev.booking.offer.domain.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OfferConfiguration {

    private val logger = LoggerFactory.getLogger(OfferConfiguration::class.java)

    @Bean
    fun businessApplicationService(
        businessRepository: BusinessRepository,
        identityFacade: IdentityFacade
    ): BusinessApplicationService {
        logger.info("Configuring BusinessApplicationService")
        return BusinessApplicationService(businessRepository, identityFacade)
    }

    @Bean
    fun serviceApplicationService(
        serviceRepository: ServiceRepository,
        businessApplicationService: BusinessApplicationService,
        serviceAssignmentRepository: ServiceAssignmentRepository,
        identityFacade: IdentityFacade
    ): ServiceApplicationService {
        logger.info("Configuring ServiceApplicationService")
        return ServiceApplicationService(
            serviceRepository,
            businessApplicationService,
            serviceAssignmentRepository,
            identityFacade
        )
    }

    @Bean
    fun staffApplicationService(
        staffMemberRepository: StaffMemberRepository,
        businessApplicationService: BusinessApplicationService,
        identityFacade: IdentityFacade,
        serviceAssignmentRepository: ServiceAssignmentRepository
    ): StaffApplicationService {
        logger.info("Configuring StaffApplicationService")
        return StaffApplicationService(
            staffMemberRepository,
            businessApplicationService,
            identityFacade,
            serviceAssignmentRepository
        )
    }

    @Bean
    fun staffServiceAssignmentApplicationService(
        serviceAssignmentRepository: ServiceAssignmentRepository,
        businessApplicationService: BusinessApplicationService,
        serviceApplicationService: ServiceApplicationService,
        staffApplicationService: StaffApplicationService,
        identityFacade: IdentityFacade
    ): StaffServiceAssignmentApplicationService {
        logger.info("Configuring StaffServiceAssignmentApplicationService")
        return StaffServiceAssignmentApplicationService(
            serviceAssignmentRepository,
            businessApplicationService,
            serviceApplicationService,
            staffApplicationService,
            identityFacade
        )
    }

    @Bean
    fun offerFacade(
        businessApplicationService: BusinessApplicationService,
        serviceApplicationService: ServiceApplicationService,
        staffApplicationService: StaffApplicationService,
        staffServiceAssignmentApplicationService: StaffServiceAssignmentApplicationService
    ): OfferFacade {
        logger.info("Configuring OfferFacade")
        return DefaultOfferFacade(
            businessApplicationService,
            serviceApplicationService,
            staffApplicationService,
            staffServiceAssignmentApplicationService
        )
    }
}