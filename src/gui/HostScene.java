package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import session.*;

/**
 * The active session scene for voting and changing what the clients see
 * 
 * @author Nic / Josh
 *
 */
public class HostScene extends Scene {

	private static BorderPane rootHost = new BorderPane();
	private static Pane pane = new Pane();
	private ScrollPane picPane;
	private HBox pics;
	private FileChooser fc;
	private Session s;
	private File file;
	private Button next;
	private int picIndex = 0;
	private VotoMenuBar mb;
	private ArrayList<String> questions;
	private int questionIndex = 0;
	private ArrayList<ImageView> imageList;
	private int imageIndex = 0;
	private double totalWidth = 0, currentSPposition = 0;

	/**
	 * The scene that will display the images and is essentially the active
	 * scene for the program
	 * 
	 * @param se
	 *            The active session for the scene
	 * @param width
	 *            The width of the scene
	 * @param height
	 *            The height of the scene
	 * @param mb
	 *            The menubar to be passed in with the scene
	 */
	public HostScene(Session se, double width, double height, VotoMenuBar mb) {
		super(pane, width, height);

		pane.setPrefHeight(height);
		pane.setPrefWidth(width);

		rootHost.setPrefHeight(height);
		rootHost.setPrefWidth(width);

		questions = new ArrayList<>();
		this.mb = mb;

		s = se;

		// Instantiate new GUI elements
		picPane = new ScrollPane();
		picPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		// picPane.setFitToHeight(true);
		// picPane.setMinHeight(200);
		pics = new HBox();

		picPane.setContent(pics);

		pane.getChildren().add(rootHost);

		next = new Button("Next");
		next.setOnAction(e -> {
			nextQuestion();
			mb.updateQuestionList();
		});
		next.setLayoutX(width - 80);
		next.setLayoutY(height - 55);

		pane.getChildren().add(next);
		imageList = new ArrayList<>();
	}

	/**
	 * Opens images from file, sets session name, builds GUI
	 * 
	 * @return
	 */
	public File start() {
		mb.setOpenFile(e -> openFile());
		mb.setSession(e -> {
			nextQuestion();
			mb.updateQuestionList();
		}, e -> stopSession());
		File file = openFile();

		if (file != null) {
			s.setID(file.getName().substring(0, file.getName().length() - 5));
		}

		rootHost.setTop(mb);
		rootHost.setCenter(picPane);

		for (int i = 0; i < questions.size(); i += 2) {
			if (!addPic(questions.get(i))) {
				return null;
			}
		}

		if (imageList.size() <= 1) {
			next.setText("Done");
			mb.setNext("Done");
		}

		return file;
	}

	/**
	 * Open picture from file chooser to host pane
	 */
	private File openFile() {

		// Instantiate
		fc = new FileChooser();
		fc.setInitialDirectory(new File("./"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("VOTO files (*.voto)", "*.voto");
		fc.getExtensionFilters().add(extFilter);
		file = fc.showOpenDialog(null);

		Scanner txtScan = null;
		String filePath = null;
		String answer = null;

		if (file != null && file.getPath().endsWith(".voto")) {
			try {
				txtScan = new Scanner(file);
				while (txtScan.hasNext()) {
					filePath = txtScan.nextLine();
					answer = txtScan.nextLine();
					// addPic(filePath);
					if (questionIndex == 0) {
						s.setCurrentQuestion(filePath, answer);
						questionIndex += 2;
						questions.add(filePath);
						questions.add(answer);
					} else {
						questions.add(filePath);
						questions.add(answer);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * Opens picture and loads into an ImageView
	 * 
	 * @param filepath
	 *            The filepath of the image to be loaded
	 */
	private boolean addPic(String filepath) {
		ImageView iView = null;
		File currentFile = new File(filepath);
		try {
			// Instantiate image view from picture
			BufferedImage bImage = ImageIO.read(currentFile);
			Image image = SwingFXUtils.toFXImage(bImage, null);
			iView = new ImageView();
			iView.setImage(image);
			imageList.add(iView);
		} catch (IOException e) {
			// File image error
			e.printStackTrace();
			reset();
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("File Error");
			alert.setHeaderText(null);
			Optional<ButtonType> result = alert.showAndWait();
			return false;
		}
		// add images to GUI
		addImgToSP(iView);
		if (imageIndex == 0) {
			imageList.get(imageIndex).setFitHeight(pane.getHeight() - mb.getHeight() - 2);
			imageIndex++;
		}
		return true;
	}

	/**
	 * Adds image to the HBox
	 * 
	 * @param iViewPrev
	 *            The image to be added as ImageView
	 */
	private void addImgToSP(ImageView iViewPrev) {
		iViewPrev.setPreserveRatio(true);
		iViewPrev.setFitHeight(pane.getHeight() - mb.getHeight() - 30); // All
																		// images
																		// smaller
																		// than
																		// current
		pics.getChildren().add(picIndex, iViewPrev);
		picIndex++;
		totalWidth += iViewPrev.getBoundsInParent().getWidth();
	}

	/**
	 * Moves onto the nextQuestion in the session
	 */
	private void nextQuestion() {
		if (questionIndex < questions.size()) {
			s.setCurrentQuestion(questions.get(questionIndex), questions.get(++questionIndex));
			imageList.get(imageIndex).setFitHeight(pane.getHeight() - mb.getHeight() - 2);
			imageList.get(imageIndex - 1).setFitHeight(pane.getHeight() - mb.getHeight() - 30);

			if (imageIndex + 1 == imageList.size()) {
				picPane.setHvalue(1);
			} else {
				currentSPposition += imageList.get(imageIndex).getBoundsInParent().getWidth() / totalWidth;
				picPane.setHvalue(currentSPposition);
			}

			imageIndex++;
			if (++questionIndex == questions.size()) {
				next.setText("Done");
				mb.setNext("Done");
			}
		} else {
			stopSession();
		}
	}

	/**
	 * Ends session and resets variables
	 */
	private void stopSession() {
		s.stop();
		System.out.println("Session stopped");

		reset();

		try {
			s.save();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		s.reset();

		VotoDesktopFX.primary.setScene(VotoDesktopFX.launch);
		mb.menuBarInLaunch();
		VotoDesktopFX.launch.reinstallMenuBar(mb);

	}

	/**
	 * Reset variables at end of session
	 */
	public void reset() {
		mb.setNext("Next");
		next.setText("Next");

		pics.getChildren().clear();

		picIndex = 0;
		questionIndex = 0;
		imageIndex = 0;
		questions.clear();
		imageList.clear();
		totalWidth = 0;

		picPane.setHvalue(0);
		currentSPposition = 0;
	}

}