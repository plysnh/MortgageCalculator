/**
 * NAME: NHI PHAN
 * DATE: NOV 21ST, 2022
 * TYPE: (take home) MIDTERM CMPT 305
 * Reference:
 * - Add object to style in CSS file: https://stackoverflow.com/questions/44404869/
 *                                  how-to-create-and-add-a-style-class-dynamically-in-javafx
 * - JDIALOG: https://docs.oracle.com/javase/7/docs/api/javax/swing/JDialog.html
 * - Hide Horizontal Scroll: https://stackoverflow.com/questions/37450903/horizontal-
 *                             scroll-bar-hides-the-last-row-in-a-tableview-javafx
 * - https://stackoverflow.com/questions/5710394/how-do-i-round-a-double-to-two-decimal-places-in-java
 */
package chart;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Formatter;

import static java.lang.String.*;
import static javafx.scene.paint.Color.*;

public class CompoundInterestCalculator extends Application {

	private BarChart<String, Number> barChart;
	private TableView<Year> table;
	private ObservableList<Year> interestPerYearList = FXCollections.observableArrayList();
	private Button calculateBtn, scheduleBtn;
	private VBox vBoxInput; //This vbox on the upper left to get user input
	private TextField principalField, annualInterestField, numberOfYearField;
	private TextField downPaymentField;
	 private Text text1, text2, text3, text4; //Result of Morgate Calculator
	 private Label payment, totalLabel, interestTotalLabel;
	private Pane resultPane;
	private String principal, interest, years, downPayment, monthlyPayment;
	private String totalAmountPaid, interestPaid;


	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Mortgage Calculator");
		configureTable();

		Label label = new Label("Mortgage Calculator");
		label.setFont(new Font(20));
		label.setStyle("-fx-font-weight: bold");

		VBox vBox = new VBox(10);
		vBox.setPadding(new Insets(10, 10, 10, 10));
		Scene scene = new Scene(vBox, 550, 650);
		//CSS will remove horizontal scroll bar
		scene.getStylesheets().add("chart/Table.css");
		primaryStage.setScene(scene);

		configureTable(); //Set up the Table View that will be in HBox
		buildVBoxInput(); //Set up Vbox that takes inPut
		buildResultPane();
		buildChart();

		VBox vBoxTab = new VBox(10);
		//vBoxTab.setPadding(new Insets(10, 10, 10, 10));
		vBoxTab.getChildren().addAll(resultPane, table);

		TabPane tabPane = new TabPane();
		Tab tableTab = new Tab("Details");
		tableTab.setContent(vBoxTab);
		tabPane.getTabs().add(tableTab);
		Tab chartTab = new Tab("Chart");
		chartTab.setContent(barChart);
		tabPane.getTabs().add(chartTab);

		vBox.getChildren().addAll(label,vBoxInput,tabPane);
		primaryStage.show();
		calculateBtn();


	}

	/**
	 * PUBLIC FUNCTION
	 */

	public static void main(String[] args) {
		launch(args);
	}
	/** FROM HERE BELOW IS HELPER FUNCTION**/



	/**
	 * This helper function check the text field condition everytime the
	 * Calculate Button is clicked
	 * The conditions are:
	 * 1. All field must be numeric
	 * 2. Number of year must be positive integer (not double)
	 */
	private void calculateBtn(){
		//If the Calculate button is clicked (Action)
		calculateBtn.setOnAction(event -> {
			//We clear the observable list and bar chart before display new search/error box
			interestPerYearList.clear();
			barChart.getData().clear();

			principal = principalField.getText();
			interest = annualInterestField.getText();
			downPayment = downPaymentField.getText();
			years = numberOfYearField.getText();

			if (!isNumeric(principal)) {
				errorHandler("Please enter a valid principal amount!");
			}
			else if (!isValidInterestRate(interest)) {
				errorHandler("Please enter a valid interest rate!");
			}
			//"Compounding Frequency" must not be null (user has to pick an option)
			else if (!isNumeric(downPayment)){
				errorHandler("Please enter a down payment amount. Recommend at least 20%.");
			}
			// "number of year" must be numeric and positive
			else if (!isValidYear(years)){
				errorHandler("Please enter a valid number of year!");
			}
			// If all textfield and combox are filled correctly
			else{
				drawChart(getData());
			}

			updateResultPane();
		});
	}

	/**
	 * GetData function will calculate interest earned every year,
	 * closing balance for that year.
	 * Note: The opening balance of this year is the closing balance of the
	 * year before.
	 * @return interestPerYearList which is ObservableList<Year> to
	 *          pass to BarChart and draw it
	 */
	private ObservableList<Year> getData() {
		double downPayment = Double.parseDouble(downPaymentField.getText());

		//Extract totalYear, annual rate and opening balance of the first year
		int totalYear = Integer.parseInt(numberOfYearField.getText());
		double r = Double.parseDouble(annualInterestField.getText());
		double principal = Double.parseDouble(principalField.getText());
		int i = 0;
		double openingBalance = principal - downPayment;
		//create Year object: first Year, then proceed to calculate each year
		Year firstYear = new Year(i+1,openingBalance,r,downPayment, totalYear, principal);

		monthlyPayment = valueOf(firstYear.getFixedMonthlyPayment());
		totalAmountPaid = valueOf(firstYear.getAnnualPayment() * totalYear);
		interestPaid = valueOf(firstYear.getAnnualPayment() * totalYear - openingBalance);

		interestPerYearList.add(firstYear);
		while (i < totalYear - 1){
			Year previousYear = interestPerYearList.get(i);
			openingBalance = previousYear.getClosingBalance();
			i++;
			//System.out.println("Opening Balance of year " + (i+1) + " : "+ openingBalance); //dev test
			Year nextYear = new Year (i+1, openingBalance, r, downPayment, totalYear, principal);
			interestPerYearList.add(nextYear);
		}
		return interestPerYearList;
	}


	private void buildResultPane(){
		resultPane = new Pane();
		resultPane.setStyle("-fx-background-color: #003366;");
		resultPane.setPrefSize(200,200);

		text1 = new Text(); text2 = new Text();
		text3 = new Text(); text4 = new Text();
		payment = new Label ("");
		totalLabel = new Label("");
		interestTotalLabel = new Label("");
		resultPane.getChildren().addAll(text1, text2, text3, text4, payment, totalLabel, interestTotalLabel);
	}

	/**
	 *
	 */

	private void updateResultPane(){

		//Round a double to two decimal places ex:  659.9557392166588 becomes 659.96
		double test = (Math.round(Double.parseDouble(monthlyPayment) * 100))/100.00;

		//Use Formatter to convert double to currency display
		Formatter amount = new Formatter();
		amount.format("%,.2f", test);

		//FinalPrincipal is the loan which = principal - downpayment
		Formatter finalPrincipal = new Formatter();
		finalPrincipal.format("%,.2f", Double.parseDouble(principal)-Double.parseDouble(downPayment));

		Formatter totalPaid = new Formatter();
		totalPaid.format("%,.0f", Double.parseDouble(totalAmountPaid));

		Formatter interestFormat = new Formatter();
		interestFormat.format("%,.0f", Double.parseDouble(interestPaid));

		//Create Label and Text
		String result1 = "To pay off a loan of " + finalPrincipal + " in " + years +
							" years at an annual interest rate of " + interest + "%";
		String result2 = "Your monthly payment would be ";
		text1.setText(result1);
		text1.setFill(WHITE);
		text1.setFont(new Font(15));
		text2.setText(result2);
		text2.setFill(WHITE);
		text2.setFont(new Font(15));
		text3.setText("Total Amount Paid");
		text3.setFill(WHITE);
		text3.setFont(new Font(14));
		text4.setText("Interest Paid");
		text4.setFill(WHITE);
		text4.setFont(new Font(14));

		//set up GUI of the Currency display $
		payment.setText("$" + amount);
		payment.setFont(new Font(20));
		payment.setStyle("-fx-font-weight: bold");  payment.setTextFill(WHITE);

		totalLabel.setText("$" + totalPaid);
		totalLabel.setFont(new Font(16));
		totalLabel.setStyle("-fx-font-weight: bold");
		totalLabel.setTextFill(WHITE);

		interestTotalLabel.setText("$" + interestFormat);
		interestTotalLabel.setFont(new Font(16));
		interestTotalLabel.setStyle("-fx-font-weight: bold");
		interestTotalLabel.setTextFill(WHITE);

		//Relocate the texts and label according from top down
		text1.relocate(20, 15);
		text2.relocate(20, 35);
		payment.relocate(20, 55);
		text3.relocate(20, 90);
		totalLabel.relocate(20, 110);
		text4.relocate(20, 140);
		interestTotalLabel.relocate(20, 160);

	}
	/**
	 * buildVBoxInput() will set up all the element of the Upper Left Part
	 * where user will input in information.
	 */
	private void buildVBoxInput(){
		vBoxInput = new VBox();
		GridPane gridPane = new GridPane();

		// spacing the buttons and text fields so that it get equally 50% of width
		gridPane.setVgap(10);
		gridPane.setHgap(10);
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(25);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(25);
		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(25);
		gridPane.getColumnConstraints().addAll(column1, column2, column3);

		// Create Label and Text Field to add to GridPane
		Label principal = new Label("Principal ($):");
		Label annualInterest = new Label("Annual Interest (%):");
		Label downPayment = new Label("Down Payment ($):");
		Label numberOfYear = new Label("Number Of Year:");
		principalField = new TextField();
		annualInterestField = new TextField();
		numberOfYearField = new TextField();
		downPaymentField = new TextField();

		// Calculate Button
		calculateBtn = new Button("Calculate");

		// adding labels, textField, combobox to the gridPane
		gridPane.add(principal, 0, 0);
		gridPane.add(principalField, 1, 0);
		gridPane.add(annualInterest, 2, 0);
		gridPane.add(annualInterestField, 3, 0);
		gridPane.add(downPayment, 0, 1);
		gridPane.add(downPaymentField, 1, 1);
		gridPane.add(numberOfYear, 2, 1);
		gridPane.add(numberOfYearField, 3, 1);
		gridPane.add(calculateBtn, 1, 2);
		//All Grid Pane to VBox
		vBoxInput.getChildren().addAll(gridPane);

	}

	/**
	 * ErrorHandler()
	 * @param message: the message that will appear in the JDialog Box
	 *               which will change depend on the errors.
	 */
	private void errorHandler(String message){
		JDialog dialog = new JDialog();
		dialog.setTitle("Input Value Error");
		dialog.setSize(400, 150);
		dialog.setLayout(new FlowLayout()); // to show label and button
		dialog.setLocationRelativeTo(null); //make Jdialog pop up in the middle

		JButton button = new JButton("OK");
		//If "OK" button is clicked, close the JDialog
		button.addActionListener ( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				dialog.setVisible(false);
			}
		});
		//Else, Make all elements in dialog visible
		button.setVisible(true);
		dialog.add(new JLabel( message));
		dialog.add(button);
		dialog.setVisible(true);
	}
	/**
	 * buildChart () will set up all the element of the Bar Chart
	 */
	private void buildChart() {
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Year");
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Principal Repayment (%)");
		barChart = new BarChart<>(xAxis, yAxis);
		barChart.setTitle("Percent of Payment that goes into Principal Over Year");
		barChart.setAnimated(false);
		barChart.setLegendVisible(false);
	}

	/**
	 * Helper function to draw Bar chart
	 * Pre-condition: have at least one element in the data
	 * @param data : is an Observable List of Year element.
	 * NOTE:  "XYChart.Series<String, Number> series"  will
	 *            also take in String, Number, so convert
	 *            Year into String (in Year Class)
	 */
	private void drawChart(ObservableList<Year> data) {
		barChart.getData().clear();
		XYChart.Series<String, Number> series = new XYChart.Series<>();

		for (Year year : data) {
			series.getData().add(new XYChart.Data<>(valueOf(year.getYear()), year.getPercentOfPaymentGoesIntoPrincipal()));
		}
		barChart.getData().add(series);
	}


	/**
	 * IsNumeric function to check if Principal Amount is numeric (int, double)
	 * Principle can be negative (ex: loans)
	 * @param s: Principal amount
	 * @return boolean value
	 */
	private static boolean isNumeric(String s){
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e){
			return false;
		}
	}
	/**
	 * IsValidYear function to check if Year is a positive int
	 * @param s : Year
	 * @restraint s is smaller than 1000 years
	 * @return boolean value
	 */
	private static boolean isValidYear (String s){
		try {
			int year = Integer.parseInt(s);
			return year > 0 && year < 1000;
		} catch (NumberFormatException e){
			return false;
		}
	}
	/**
	 * IsValidInterestRate function to check if interest is a positive double
	 * @param s : Interest Rate
	 * @restraint s is smaller than 99% (unrealistic to have anything above that)
	 * @return boolean value
	 */
	private static boolean isValidInterestRate(String s){
		try {
			double val = Double.parseDouble(s);
			return val >= 0 && val < 99;
		} catch (NumberFormatException e){
			return false;
		}
	}


	/**
	 * Set up the Table View that has 4 columns
	 */
	private void configureTable() {
		table = new TableView<>();
		interestPerYearList = FXCollections.observableArrayList();
		table.setItems(interestPerYearList);
		table.setMaxHeight(250);

		//COLUMN 1: YEAR
		TableColumn<Year, Integer> yearCol = new TableColumn<>("Year");
		yearCol.setCellValueFactory(new PropertyValueFactory<>("Year"));
		yearCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
		table.getColumns().add(yearCol);

		//COLUMN 2: OPENING BALANCE
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		TableColumn<Year, Double> openingBalanceCol = new TableColumn<>("Opening Balance");
		openingBalanceCol.setCellValueFactory(new PropertyValueFactory<>("OpeningBalance"));
		openingBalanceCol.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				currencyFormat.setMaximumFractionDigits(2);
				setText(empty ? "" : currencyFormat.format(value));
			}
		});
		openingBalanceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		table.getColumns().add(openingBalanceCol);

		//COLUMN 3: INTEREST
		TableColumn<Year, Double> interestCol = new TableColumn<>("Annual Payment");
		interestCol.setCellValueFactory(new PropertyValueFactory<>("AnnualPayment"));
		interestCol.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				currencyFormat.setMaximumFractionDigits(2);
				setText(empty ? "" : currencyFormat.format(value));
			}
		});
		interestCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		table.getColumns().add(interestCol);

		//COLUMN 4: PRINCIPAL REPAYMENT
		TableColumn<Year, Double> principalRepaymentCol = new TableColumn<>("Principal Paid");
		principalRepaymentCol.setCellValueFactory(new PropertyValueFactory<>("PrincipalRepayment"));
		principalRepaymentCol.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				currencyFormat.setMaximumFractionDigits(2);
				setText(empty ? "" : currencyFormat.format(value));
			}
		});
		principalRepaymentCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20));
		table.getColumns().add(principalRepaymentCol);

		//COLUMN 5: CLOSING BALANCE
		TableColumn<Year, Double> closingBalanceCol = new TableColumn<>("Closing Balance");
		closingBalanceCol.setCellValueFactory(new PropertyValueFactory<>("ClosingBalance"));
		closingBalanceCol.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				currencyFormat.setMaximumFractionDigits(2);
				setText(empty ? "" : currencyFormat.format(value));
			}
		});
		closingBalanceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20));
		table.getColumns().add(closingBalanceCol);
	}
}

/**************************************THE END*************************************************/

