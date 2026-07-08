package com.mycompany.myapp.service.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardDTO {

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalFbRevenue = BigDecimal.ZERO;
    private BigDecimal averageOccupancyRate = BigDecimal.ZERO;
    private BigDecimal comboAttachRate = BigDecimal.ZERO;
    private BigDecimal averageMovieRating = BigDecimal.ZERO;
    private long totalTicketsSold;
    private long totalUsers;
    private long paidInvoicesCount;
    private long invoicesWithCombo;
    private long reviewCount;
    private MetricComparisonDTO revenueComparison = new MetricComparisonDTO();
    private MetricComparisonDTO fbRevenueComparison = new MetricComparisonDTO();
    private MetricComparisonDTO ticketsComparison = new MetricComparisonDTO();
    private MetricComparisonDTO usersComparison = new MetricComparisonDTO();
    private List<DashboardItemDTO> topMovies = new ArrayList<>();
    private List<DashboardItemDTO> topRatedMovies = new ArrayList<>();
    private List<DashboardItemDTO> topRooms = new ArrayList<>();
    private List<DashboardItemDTO> topCombos = new ArrayList<>();
    private List<DashboardItemDTO> statusBreakdown = new ArrayList<>();
    private List<DashboardItemDTO> peakHours = new ArrayList<>();
    private List<DashboardItemDTO> ticketTypeBreakdown = new ArrayList<>();

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalFbRevenue() {
        return totalFbRevenue;
    }

    public void setTotalFbRevenue(BigDecimal totalFbRevenue) {
        this.totalFbRevenue = totalFbRevenue;
    }

    public BigDecimal getAverageOccupancyRate() {
        return averageOccupancyRate;
    }

    public void setAverageOccupancyRate(BigDecimal averageOccupancyRate) {
        this.averageOccupancyRate = averageOccupancyRate;
    }

    public BigDecimal getComboAttachRate() {
        return comboAttachRate;
    }

    public void setComboAttachRate(BigDecimal comboAttachRate) {
        this.comboAttachRate = comboAttachRate;
    }

    public BigDecimal getAverageMovieRating() {
        return averageMovieRating;
    }

    public void setAverageMovieRating(BigDecimal averageMovieRating) {
        this.averageMovieRating = averageMovieRating;
    }

    public long getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getPaidInvoicesCount() {
        return paidInvoicesCount;
    }

    public void setPaidInvoicesCount(long paidInvoicesCount) {
        this.paidInvoicesCount = paidInvoicesCount;
    }

    public long getInvoicesWithCombo() {
        return invoicesWithCombo;
    }

    public void setInvoicesWithCombo(long invoicesWithCombo) {
        this.invoicesWithCombo = invoicesWithCombo;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public MetricComparisonDTO getRevenueComparison() {
        return revenueComparison;
    }

    public void setRevenueComparison(MetricComparisonDTO revenueComparison) {
        this.revenueComparison = revenueComparison;
    }

    public MetricComparisonDTO getFbRevenueComparison() {
        return fbRevenueComparison;
    }

    public void setFbRevenueComparison(MetricComparisonDTO fbRevenueComparison) {
        this.fbRevenueComparison = fbRevenueComparison;
    }

    public MetricComparisonDTO getTicketsComparison() {
        return ticketsComparison;
    }

    public void setTicketsComparison(MetricComparisonDTO ticketsComparison) {
        this.ticketsComparison = ticketsComparison;
    }

    public MetricComparisonDTO getUsersComparison() {
        return usersComparison;
    }

    public void setUsersComparison(MetricComparisonDTO usersComparison) {
        this.usersComparison = usersComparison;
    }

    public List<DashboardItemDTO> getTopMovies() {
        return topMovies;
    }

    public void setTopMovies(List<DashboardItemDTO> topMovies) {
        this.topMovies = topMovies;
    }

    public List<DashboardItemDTO> getTopRatedMovies() {
        return topRatedMovies;
    }

    public void setTopRatedMovies(List<DashboardItemDTO> topRatedMovies) {
        this.topRatedMovies = topRatedMovies;
    }

    public List<DashboardItemDTO> getTopRooms() {
        return topRooms;
    }

    public void setTopRooms(List<DashboardItemDTO> topRooms) {
        this.topRooms = topRooms;
    }

    public List<DashboardItemDTO> getTopCombos() {
        return topCombos;
    }

    public void setTopCombos(List<DashboardItemDTO> topCombos) {
        this.topCombos = topCombos;
    }

    public List<DashboardItemDTO> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(List<DashboardItemDTO> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public List<DashboardItemDTO> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<DashboardItemDTO> peakHours) {
        this.peakHours = peakHours;
    }

    public List<DashboardItemDTO> getTicketTypeBreakdown() {
        return ticketTypeBreakdown;
    }

    public void setTicketTypeBreakdown(List<DashboardItemDTO> ticketTypeBreakdown) {
        this.ticketTypeBreakdown = ticketTypeBreakdown;
    }

    public static class MetricComparisonDTO {

        private BigDecimal previousValue = BigDecimal.ZERO;
        private BigDecimal deltaValue = BigDecimal.ZERO;
        private BigDecimal deltaPercent = BigDecimal.ZERO;

        public BigDecimal getPreviousValue() {
            return previousValue;
        }

        public void setPreviousValue(BigDecimal previousValue) {
            this.previousValue = previousValue;
        }

        public BigDecimal getDeltaValue() {
            return deltaValue;
        }

        public void setDeltaValue(BigDecimal deltaValue) {
            this.deltaValue = deltaValue;
        }

        public BigDecimal getDeltaPercent() {
            return deltaPercent;
        }

        public void setDeltaPercent(BigDecimal deltaPercent) {
            this.deltaPercent = deltaPercent;
        }
    }

    public static class DashboardItemDTO {

        private String name;
        private long count;
        private long capacity;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal percentage = BigDecimal.ZERO;
        private BigDecimal averageRating = BigDecimal.ZERO;

        public DashboardItemDTO() {}

        public DashboardItemDTO(String name, long count, BigDecimal revenue) {
            this.name = name;
            this.count = count;
            this.revenue = revenue;
        }

        public DashboardItemDTO(String name, long count, long capacity, BigDecimal revenue, BigDecimal percentage) {
            this.name = name;
            this.count = count;
            this.capacity = capacity;
            this.revenue = revenue;
            this.percentage = percentage;
        }

        public DashboardItemDTO(
            String name,
            long count,
            long capacity,
            BigDecimal revenue,
            BigDecimal percentage,
            BigDecimal averageRating
        ) {
            this.name = name;
            this.count = count;
            this.capacity = capacity;
            this.revenue = revenue;
            this.percentage = percentage;
            this.averageRating = averageRating;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }

        public BigDecimal getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(BigDecimal averageRating) {
            this.averageRating = averageRating;
        }
    }
}
