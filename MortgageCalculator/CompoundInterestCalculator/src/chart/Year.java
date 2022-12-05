package chart;

import static java.lang.Math.abs;

/**
 * CLASS YEAR: contains information about each year entry:
 * the year(th)
 * The openning Balance of year(th)
 * The closing Balance of year(th)
 * The annual Interest Rate of all the year
 */
public class Year {
    private final int year;
    private final double openingBalance;
    private double fixedMonthlyPayment, annualPayment;
    private final double closingBalance;
    private double principalRepayment;  //The amount of payment that goes towards paying the principal
    private int percentOfPaymentGoesIntoPrincipal;

    /**
     *
     * @param year : the year(th) entry
     * @param openingBalance : the beginning balance of year(th)
     * @param annualInterestRate : the annual interest
     */
    public Year(int year, double openingBalance, double annualInterestRate, double downPayment, int totalYear, double principal){
        this.year = year;
        this.openingBalance = openingBalance;
        int n = year*12;
        double r = annualInterestRate/(12*100); //rate per month
        principal = principal - downPayment;  // if there is downpayment
        Double powerRate = Math.pow((1+r),12*totalYear) - Math.pow((1+r),n);
        //System.out.println("power Rate (1+r)^n = " + powerRate);
        Double secondPart = Math.pow((1+r),12*totalYear) - 1;
        Double powerMonth = Math.pow((1+r),12*totalYear);
        fixedMonthlyPayment = principal * r * powerMonth / (powerMonth - 1);
        annualPayment = fixedMonthlyPayment * 12;
        //System.out.println("Monthly Payment: " + fixedMonthlyPayment + " month: "+ year );
        double annualPayment = 12 * fixedMonthlyPayment;
        double interestPaid = annualInterestRate * openingBalance;
        double principalRepayment = annualPayment - interestPaid;
        //System.out.println("OPenning balance is : " + openingBalance);
        this.closingBalance = principal * powerRate / secondPart;
        this.principalRepayment = openingBalance - closingBalance;
        this.percentOfPaymentGoesIntoPrincipal = (int) Math.round((this.principalRepayment / annualPayment) * 100);
    }

    /**
     * GETTER METHOD
     * @return
     */
    public int getYear(){
        return this.year;
    }
    public int getPercentOfPaymentGoesIntoPrincipal(){return this.percentOfPaymentGoesIntoPrincipal;}
    public double getFixedMonthlyPayment(){
        return this.fixedMonthlyPayment;
    }
    public double getAnnualPayment(){
        return this.annualPayment;
    }

    public double getOpeningBalance(){
        return this.openingBalance;
    }
    public double getPrincipalRepayment() {return this.principalRepayment;}
    public double getClosingBalance(){
        return this.closingBalance;
    }
}
