package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderStatsDTO {

    // Order counts by status
    private Long totalOrders;
    private Long pendingPaymentOrders;
    private Long paidOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long refundedOrders;

    // Financial statistics
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    private BigDecimal averageOrderValue;

    // Order processing metrics
    private Double averageProcessingTimeHours;
    private Double averageShippingTimeHours;
    private Double orderFulfillmentRate;

    // Top performing items
    private Map<String, Integer> topSellingBooks;
    private Map<String, BigDecimal> topRevenueBooks;

    // Customer insights
    private Long uniqueCustomers;
    private Long repeatCustomers;
    private Double customerRetentionRate;

    // Daily/Weekly trends
    private Map<String, Long> dailyOrderCounts;
    private Map<String, BigDecimal> dailyRevenue;

    // Alerts and actions needed
    private Long ordersNeedingAttention;
    private Long overdueShipments;
    private Long paymentIssues;
}