package agent;

public interface RestoAgentInterface {
	public void handleSpoken(String s);
	public void handleMensajeParaMiMismo(String s);
	public String[] getParticipantNames();
}
