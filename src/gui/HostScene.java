package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import session.*;

public class HostScene extends Scene {

	private static BorderPane rootHost = new BorderPane();
	private ScrollPane picPane;
	private VBox pics;
	private FlowPane centerPic;
	private GridPane hostGrid;
	private FileChooser fc;
	private Session s;

	public HostScene(Session se, double width, double height) {
		super(rootHost, width, height);

		s = se;
		
		//Instantiate new GUI elements
		picPane = new ScrollPane();
		picPane.setMinWidth(120);
		pics = new VBox();
		picPane.setContent(pics);
		centerPic = new FlowPane();

		Button open = new Button("Open File");
		open.setOnAction(e -> openFile());	//Add action listener to open file chooser
		hostGrid = new GridPane();

		//Add IP address
		try {
			hostGrid.add(new Label(InetAddress.getLocalHost().getHostAddress()), 0, 0);
		}
		catch (UnknownHostException ue) {
			System.exit(1);
		}

		//Add elements to stage
		hostGrid.add(open, 0, 1);

		rootHost.setLeft(hostGrid);
		rootHost.setCenter(centerPic);
		rootHost.setRight(picPane);
	}


	/**
	 * Open picture from file chooser to host pane
	 */
	private void  openFile() {

		//Instantiate
		fc = new FileChooser();
		File file = fc.showOpenDialog(null);
		ImageView iView = null;

		//Load picture if one was selected
		if (file != null) {
			try {
				//Instantiate image view from picture
				BufferedImage bImage = ImageIO.read(file);
				Image image = SwingFXUtils.toFXImage(bImage, null);
				iView = new ImageView();
				iView.setImage(image);

			} catch (IOException e) {
				System.exit(1);
			}
			//Add previous image view to scrollpane 
			if (!centerPic.getChildren().isEmpty()) {
				ImageView iViewPrev = (ImageView) centerPic.getChildren().remove(0);
				iViewPrev.setFitHeight(100);
				iViewPrev.setFitWidth(100);
				pics.getChildren().add(iViewPrev);
			}

			//open image to center
			iView.setFitHeight(410);
			iView.setFitWidth(400);
			centerPic.getChildren().add(iView);
			answerStage();

			try {
				ArrayList<byte[]> qImg = s.loadImage(file.getPath());
				s.currentQuestion = new Question(s, qImg, (int)(Math.random() * 20));
				System.out.println("Loaded image size: " + s.getCurrentImageSize());
				System.out.println("Packet count: " + s.getCurrentImagePacketCount());
			} catch (Exception e) {

			}

		}		
	}

	
	//GUI for setting answer for image
		public void answerStage() {
		
			//Instantiate new elements
			FlowPane ansPane = new FlowPane();
			ansPane.setPadding(new Insets(0, 7, 7, 7));
			ansPane.getChildren().add(new Label("Set Correct Answer"));
			ansPane.setVgap(10);
			ansPane.setAlignment(Pos.CENTER);
			rootHost.setBottom(ansPane);
				
			//Create buttons
			Button[] ansButton = new Button[5];
			for (int index = 0; index < 5; index++) {
				ansButton[index] = new Button(Character.toString((char)(0x0041+index)));
				ansButton[index].setMinSize(55, 25);
				ansPane.getChildren().add(ansButton[index]);
					
				//Add correct answer to list
				ansButton[index].setOnAction(e -> {
					Object source = e.getSource();
					if (source instanceof Button) {
						    Button button = (Button) source;
						    
						    //Set correct answer
						    s.currentQuestion.setAnswer((button.getText()));
						    rootHost.getChildren().remove(ansPane);
						    
						    if (!centerPic.getChildren().isEmpty()) {
						    	ImageView iViewPrev = (ImageView) centerPic.getChildren().remove(0);
								iViewPrev.setFitHeight(100);
								iViewPrev.setFitWidth(100);
								pics.getChildren().add(iViewPrev);
						    }
					}
				});
			}
		}
}