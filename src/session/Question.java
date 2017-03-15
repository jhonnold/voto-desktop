package session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class Question {
	
	private Session currentSession;
	private int answer = (int)'A';
	private int imageID;
	private HashMap<Vote, ArrayList<Client>> answerSet = new HashMap<>();
	private HashMap<String, Vote> choices = new HashMap<>();
	public ArrayList<byte[]> questionImg = new ArrayList<byte[]>();
	
	public Question(Session s, ArrayList<byte[]> img, int imageID) {

		questionImg = img;
		currentSession = s;
		this.imageID = imageID;
		choices.put("A", new Vote(1));
		choices.put("B", new Vote(2));
		choices.put("C", new Vote(3));
		choices.put("D", new Vote(4));
		choices.put("E", new Vote(5));
		
	}
		
	public void setAnswer(int ans) { answer = ans; }
	public int imageID() { return imageID; }
	public int imageSize() { return questionImg.size(); }
	
	public void addVote(String clientID, String clientVote) {

		Client c = currentSession.getClient(clientID);
		Vote lastVote = c.getLastVote();
		
		if (lastVote != null) {
			answerSet.get(lastVote).remove(c);
		}
		
		Vote v = choices.get(clientVote);
		
		answerSet.get(v).add(c);
		c.setLastVote(v, lastVote);
	}
	
	public void endQuestion() {
		
		for (Vote v : answerSet.keySet()) {
			
			for (Client c : answerSet.get(v)) {
				
				c.setLastVote(v, null);
			}
			
		}
		
	}
	
	public byte[] getImagePacket(int packetNum) throws IllegalArgumentException {
		
		if (packetNum < 1 || packetNum >= questionImg.size() + 1) {
			throw new IllegalArgumentException("Packet number is invalid for this image");
		}
		
		return questionImg.get(packetNum - 1);
	}
	
}