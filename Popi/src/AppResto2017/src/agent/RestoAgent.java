package agent;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import chat.ontology.ChatOntology;
import chat.ontology.Joined;
import chat.ontology.Left;
import android.content.Intent;
import android.media.JetPlayer.OnJetEventListener;
import android.content.Context;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;

import java.util.Date;
import java.util.Vector;


public class RestoAgent extends Agent implements RestoAgentInterface {
	
	private ACLMessage spokenMsg;
	
	private ACLMessage pedidoMasInfoMsg;
	private static final String PEDIDOMAS_INFO = "__MASINFO__";
	
	private static final long serialVersionUID = 1594371294421614291L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	private Context context;
	
	private static final String CHAT_NEG_ID = "__chat__";
	private static final String NEG_MANAGER_NAME = "manager";

	private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology onto = ChatOntology.getInstance();
	
	
	private String mensajeAnterior="";
	private String ultimoMensajeRecibido="";
	String [] nombreDelObjeto ={"TIPOCOMIDA","BARRIO","ESTACIONAMIENTO"};
    JsonParser parser = new JsonParser();
    JsonObject objetoJsonAConstruir= new JsonObject();
 	JsonObject datosObligatorios = new JsonObject();
	boolean banderaAuxiliar=false;
	ACLMessage tmpCfp;
	
	private int precio = 1020;
	private int descuento =1;
	private int ultEstacionamiento=99;
	
	private int tipoMenuVeg=99;
	private int tipoMenuCarne=99;
	private int tipoMenuPizza=99;
	private int tipoMenuPasta=99;	
	private int estacionamineto=99;
	private int barrio=99;
	
	
	private int datoSobreObligMenu=1000;
	private int datoSobreObligBarrio=1000;
	private int datoSobreObligEstac=1000;
	
    JsonObject obj = new JsonObject();
    private String tmpObjJson="";

     
	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
			
			tmpObjJson=args[1].toString();
			obj = parser.parse(tmpObjJson).getAsJsonObject();

			precio=Integer.parseInt(obj.get("PRECIOMENU").getAsString());
			descuento=Integer.parseInt(obj.get("ULTPRECIO").getAsString());
			ultEstacionamiento=Integer.parseInt(obj.get("ULTESTAC").getAsString());
			
			estacionamineto=Integer.parseInt(obj.get("ESTACIONAMIENTO").getAsString());
			barrio=Integer.parseInt(obj.get("BARRIO").getAsString());
			tipoMenuVeg=Integer.parseInt(obj.get("MENUVEGET").getAsString());
			tipoMenuCarne=Integer.parseInt(obj.get("MENUCARNE").getAsString());
			tipoMenuPizza=Integer.parseInt(obj.get("MENUPIZZA").getAsString());
			tipoMenuPasta=Integer.parseInt(obj.get("MENUPASTA").getAsString());

			
			datoSobreObligMenu=Integer.parseInt(obj.get("DATOSOBREMENU").getAsString());
			datoSobreObligBarrio=Integer.parseInt(obj.get("DATOSOBREBARRIO").getAsString());
			datoSobreObligEstac=Integer.parseInt(obj.get("DATOSOBREESTACIONAMIENTO").getAsString());

			System.out.printf("Restaurante %s: a la espera de comensales...\n "+precio, this.getLocalName());
			addBehaviour(new Mensaje1a1(this, "\n\n Restaurante a la espera de comensales....\n\n",this.getAID()));

			  
		        //Registro del servicio de venta de menus en las páginas amarillas.
		        ServiceDescription servicio = new ServiceDescription();
		        servicio.setType("Comida");
		        servicio.setName("Menus Ejecutivos");
		 
		        DFAgentDescription descripcion = new DFAgentDescription();
		        descripcion.setName(getAID());
		        descripcion.addLanguages("Español");
		        descripcion.addServices(servicio);
		 
		        try {
		            DFService.register(this, descripcion);
		        } catch (FIPAException e) {
		            e.printStackTrace();
		        }
		 
		        //Se crea una plantilla que filtre los mensajes a recibir.
		        MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		 
		        //Añadimos los comportamientos ante mensajes recibidos
		        this.addBehaviour(new CrearOferta(this, template));
		 
			
					
		
		// Register language and ontology
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(onto);
		cm.setValidationMode(false);

		// Add initial behaviours
		addBehaviour(new ParticipantsManager(this));
		addBehaviour(new NegListener(this));

		// Initialize the message used to convey spoken sentences
		spokenMsg = new ACLMessage(ACLMessage.INFORM);
		spokenMsg.setConversationId(CHAT_NEG_ID);

		
		// Inicializo el mensaje que usare para mas INFO
		pedidoMasInfoMsg = new ACLMessage(ACLMessage.INFORM);
		pedidoMasInfoMsg.setConversationId(PEDIDOMAS_INFO);

		// Activate the GUI
		registerO2AInterface(RestoAgentInterface.class, this);
		
		Intent broadcast = new Intent();
		broadcast.setAction("jade.menu.SHOW_CHAT_NEG");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
		
		
	}
		    }//FIN DEL SETUP

		
    private class CrearOferta extends ContractNetResponder {	
    	
       	/*DataStore dStTmp=null;*/
    	ACLMessage cfp=null;
   
    	boolean termino=false;
        boolean primeraVezEjecucion=true;
    	boolean preAcuerdo=true;
    	
    	private void restaurarEstados(){
        	termino=false;
            primeraVezEjecucion=true;
        	preAcuerdo=true;    		
    	}
 
 		private WakerBehaviour solicitudMasInfo_BHV=new WakerBehaviour(this.myAgent,500L) {
 			
 			@Override
 			protected void onWake() {
					
				System.out.printf("\nSe cumplio el timepo de espera  \n");	
				System.out.printf("\nEl ultimo MensajeRecibido fue: "+ultimoMensajeRecibido+"\n"+"Mientras el que el Mensaje Anterior: "+mensajeAnterior+"\n");
				if(mensajeAnterior.equals(ultimoMensajeRecibido)){
					System.out.printf("\nPOR A \n");	

				/* Qv Fix Bug 04_09	preAcuerdo=false; */		
					preAcuerdo=true;
					JsonObject objetoJsonNuevoPartirRespuesta= new JsonObject();
					objetoJsonNuevoPartirRespuesta = parser.parse(ultimoMensajeRecibido).getAsJsonObject();
						if(datoSobreObligMenu==-1&& objetoJsonNuevoPartirRespuesta.get("TIPOCOMIDA").getAsString().equals("0"))
							preAcuerdo=false;			
						if(datoSobreObligBarrio==-1&& objetoJsonNuevoPartirRespuesta.get("BARRIO").getAsString().equals("0"))
							preAcuerdo=false;
						if(datoSobreObligEstac==-1&& objetoJsonNuevoPartirRespuesta.get("ESTACIONAMIENTO").getAsString().equals("0"))
							preAcuerdo=false;
				//Fin Fix Parte01	
				}
				else{ 

					/* Before Fix Bug 04_09 Parte 02
					preAcuerdo=true;
				    JsonObject objetoJsonNuevoPartirRespuesta= new JsonObject();
				    objetoJsonNuevoPartirRespuesta = parser.parse(ultimoMensajeRecibido).getAsJsonObject();
					for (int i=0;i<(nombreDelObjeto.length);i++){  
							if(datosObligatorios.get(nombreDelObjeto[i]).getAsInt()==1&& objetoJsonNuevoPartirRespuesta.get(nombreDelObjeto[i]).getAsString().equals("0"))
								preAcuerdo=false;
					}
					objetoJsonAConstruir=objetoJsonNuevoPartirRespuesta;
					*/	
				
					//Principio Fix Parte 02
					System.out.printf("\nPOR B \n");	
					preAcuerdo=true;
				    JsonObject objetoJsonNuevoPartirRespuesta= new JsonObject();
				    objetoJsonNuevoPartirRespuesta = parser.parse(ultimoMensajeRecibido).getAsJsonObject();

					if(datoSobreObligMenu==-1&& objetoJsonNuevoPartirRespuesta.get("TIPOCOMIDA").getAsString().equals("0"))
						preAcuerdo=false;			
					if(datoSobreObligBarrio==-1&& objetoJsonNuevoPartirRespuesta.get("BARRIO").getAsString().equals("0"))
						preAcuerdo=false;
					if(datoSobreObligEstac==-1&& objetoJsonNuevoPartirRespuesta.get("ESTACIONAMIENTO").getAsString().equals("0"))
						preAcuerdo=false;

					objetoJsonAConstruir=objetoJsonNuevoPartirRespuesta; 

					// Fin Fix Parte 02
				}
				
				termino=true;
				}
 			@Override
 			public int onEnd() {
 				System.out.printf("\n Llegue al onEnd luego de haber finalizado el tiempo de espera");
 				reset();
 				return 1;
 			};
		};

	
		private OneShotBehaviour aceptacionTrato_BHV = new OneShotBehaviour() {
				
			@Override
			public void action() {
				System.out.printf("\n\nHubo una aceptacion del trato\n\n");
			} 	
		};
		
		
		private OneShotBehaviour rechazoTrato_BHV = new OneShotBehaviour() {
			@Override
			public void action() {
                System.out.printf("\n\nHubo un rechazo del trato\n\n");
            	JsonObject contraOfertaPrecio = new JsonObject();
            	int ultimaOfertaPrecio= (int) Math.floor( (precio - (precio* descuento/100)));
            	contraOfertaPrecio.addProperty("ultOferta",ultimaOfertaPrecio);
            	contraOfertaPrecio.addProperty("ultEstacionamiento",ultEstacionamiento);
            	contraOfertaPrecio.addProperty("remitenteContraOferta", this.myAgent.getLocalName());
            	if (descuento>0 || ultEstacionamiento > estacionamineto){
                addBehaviour(new Mensaje1a1(this.myAgent, "RW_contraOferta"+"--"+contraOfertaPrecio.toString(), cfp.getSender()));
            	}
            	else{
                    addBehaviour(new Mensaje1a1(this.myAgent, "Propuesta Rechazada la que ofreció el Resto: "+getLocalName() +".\n \n \n El Restaurant: " + getLocalName()+" no esta interesado en generar un ContraOferta. Luego de que su propuesta Fue Rechazada \n", cfp.getSender()));
                    handleMensajeParaMiMismo("Fue rechazado la propuesta ofrecida al agente "+cfp.getSender().getLocalName().toString()+". Y No estoy interesado en hacerle una nueva propuesta");
            	}
			}
		};
	 
    	
        public CrearOferta(Agent agente, MessageTemplate plantilla) {
 
        	super(agente, plantilla);
            
        
            SimpleBehaviour b=new SimpleBehaviour() {

            	@Override
				public void action() {
            	
					 	
					if(primeraVezEjecucion){
						System.out.print("\n Adentro del behavouir B en la parte donde primeraEjecucion es VERDADERA \n");
				
						cfp = (ACLMessage) getDataStore().get(CFP_KEY);
						objetoJsonAConstruir = parser.parse(cfp.getContent().toString()).getAsJsonObject();
						//System.out.printf("El JSON CRUDO ES: "+cfp.getContent());

						if(datoSobreObligMenu== -1 || datoSobreObligMenu== 0) { 
							datosObligatorios.addProperty(nombreDelObjeto[0],1);		
						}
						else {
							datosObligatorios.addProperty(nombreDelObjeto[0],0);									
						}
						
						
						if(datoSobreObligBarrio== -1 || datoSobreObligBarrio== 0) { 
							datosObligatorios.addProperty(nombreDelObjeto[1],1);		
						}
						else {
							datosObligatorios.addProperty(nombreDelObjeto[1],0);									
						}

						
						if(datoSobreObligEstac== -1 || datoSobreObligEstac== 0) { 
							datosObligatorios.addProperty(nombreDelObjeto[2],1);		
						}
						else {
							datosObligatorios.addProperty(nombreDelObjeto[2],0);									
						}
						
						
						for (int i=0;i<(nombreDelObjeto.length);i++){  
							if(datosObligatorios.get(nombreDelObjeto[i]).getAsInt()==1&& objetoJsonAConstruir.get(nombreDelObjeto[i]).getAsString().equals("0"))
								preAcuerdo=false;	
						}
						
						if (!preAcuerdo){	
							addBehaviour(new Mensaje1a1(this.myAgent, "\n\n Se solicitán mas datos al agente "+cfp.getSender().getLocalName()+" para poder decidir si se realiza una propuesta o no\n\n",this.myAgent.getAID()));
							addBehaviour(new Mensaje1a1(this.myAgent, "\n\n El Restaurante "+this.myAgent.getLocalName()+" solicita más información \n\n",cfp.getSender()));
							addBehaviour(new Mensaje1a1(this.myAgent, "RW_masDatos"+"--"+datosObligatorios.toString(),cfp.getSender()));
							mensajeAnterior="RW_masDatos"+"--"+datosObligatorios.toString();
							System.out.printf("EL MENSAJE ANTERIOR QUE TENGO ES: "+mensajeAnterior);
							solicitudMasInfo_BHV.setDataStore(getDataStore());	
     			   			addBehaviour(solicitudMasInfo_BHV);
						}
						else{
							
							termino=true;					
						}
					}
					
					else{        //Es decir si no es la primera vez que se esta ejecutando la llamada a action
						//block();
					}
			} //Cierra la funcion action();
				
            	
			@Override
			public boolean done() {
				if(primeraVezEjecucion && !preAcuerdo){
	               	primeraVezEjecucion=false;
				}
				
				if(preAcuerdo && termino){
					int tmpTipoDeComida=Integer.parseInt(objetoJsonAConstruir.get("TIPOCOMIDA").getAsString());
					int tmpBarrio=Integer.parseInt(objetoJsonAConstruir.get("BARRIO").getAsString());
					int menuCompatible=0;
					int barrioCompatible=0;
					
					switch (tmpTipoDeComida){
					case 1:
						if (tipoMenuVeg==1){ menuCompatible=1;}
						break;
					case 2:	
						if (tipoMenuCarne==1){ menuCompatible=1;}
						break;
					case 3:
						if (tipoMenuPizza==1){ menuCompatible=1;}
						break;
					case 4:	
						if (tipoMenuPasta==1){ menuCompatible=1;}
						break;	
					
					case 998: //esto hace referencia que al comensal le da lo mismo cualquier tipo de menu
						menuCompatible=1;
						break;
					default:
						break;
					}
					
					if (barrio==0 || tmpBarrio==barrio || tmpBarrio==998) { barrioCompatible=1; }
					
					if (barrioCompatible==1 && menuCompatible==1) {
						
	 					ACLMessage oferta = cfp.createReply();    
						oferta.setPerformative(ACLMessage.PROPOSE);
		
						JsonObject ofertaInicial = new JsonObject();
						ofertaInicial.addProperty("propuesta_precio",precio);
						ofertaInicial.addProperty("propuesta_estacionamiento",estacionamineto);
		 
						oferta.setContent(ofertaInicial.toString());
						
							getDataStore().put(REPLY_KEY, oferta);
						
					}
					
					else {
						if (barrioCompatible==0){
					           System.out.printf("SUPUESTAMENTE NO ES COMPATIBLE EL BARRIO, el del Comensal es: "+barrio+" MIENTRA QUE EL DEL RESTO ES: "+tmpBarrio, this.myAgent.getLocalName());
					           
					           
						addBehaviour(new Mensaje1a1(this.myAgent, "RW_NOMENU" +"-- \n El Resto "+this.myAgent.getLocalName() +"No Se Encuentra en la zona buscada por el comensal \n\n",cfp.getSender()));
						//Esta Linea es nueva para brindar mayor claridad
						addBehaviour(new Mensaje1a1(this.myAgent, "El Restaurante no se encuentra en la zona buscada por el comensal "+cfp.getSender().getLocalName()+"\n\n",this.myAgent.getAID()));
						} else {
							addBehaviour(new Mensaje1a1(this.myAgent, "RW_NOMENU" +"-- \n El Resto "+this.myAgent.getLocalName() +" no posee un menu de comida compatible en referencia al solicitado \n\n",cfp.getSender()));							
						//Esta Linea es nueva para brindar mayor claridad	
							addBehaviour(new Mensaje1a1(this.myAgent, "No se posee un menu compatible con el tipo de comida buscada por el comensal "+cfp.getSender().getLocalName()+"\n\n",this.myAgent.getAID()));
					           System.out.printf("el del Comensal es: "+tmpTipoDeComida+"EL DEL RESTO VEG: "+tipoMenuVeg+" CARNE:"+tipoMenuCarne+" PIZZA:"+tipoMenuPizza+" PASTA:"+tipoMenuPasta, this.myAgent.getLocalName());

						}
						try {
							throw new RefuseException("No tengo menu compatible para el comensal"+CrearOferta.this.myAgent.getLocalName()+" me retiro de la posible negociación");
						} catch (RefuseException e) {
							e.printStackTrace();
						}

					}
						
					}
					
					if ((!preAcuerdo)&& termino){
						//Esta Linea de Aca es nueva la agrego para mayor claridad de lo que esta pasando
						addBehaviour(new Mensaje1a1(this.myAgent, "\n\n Me retiro de la negociación en referencia al agente "+cfp.getSender().getLocalName()+"\n\n",this.myAgent.getAID()));
						addBehaviour(new Mensaje1a1(this.myAgent, "RW_WITHDRAW",cfp.getSender()));
						try {
							throw new RefuseException("Debido a la falta de cooperación no estoy el Resto"+CrearOferta.this.myAgent.getLocalName()+" me retiro de la posible negociación");
						} catch (RefuseException e) {
							e.printStackTrace();
						}

						
						}
					if(termino){
						restaurarEstados();
						reset();
						return true;
					}
					else{
						return false;
					}
					
				
				}
            };
	
			b.setDataStore(getDataStore());
            registerHandleCfp(b);
            aceptacionTrato_BHV.setDataStore(getDataStore());
            registerHandleAcceptProposal(aceptacionTrato_BHV);
            rechazoTrato_BHV.setDataStore(getDataStore());
            registerHandleRejectProposal(rechazoTrato_BHV);
            
        }

    }


	
	
	
	
	
	// ///////////////////////////////////////
	// MeTodOs llamados por la Interface
	// ///////////////////////////////////////
	public void handleSpoken(String s) {
		addBehaviour(new NegNotification(this, s));
	}
	
	public void handleMensajeParaMiMismo(String s) {
		addBehaviour(new Mensaje1a1(this,s ,this.getAID()));
	}
	/////////////////////////////////////////////
	
	
	private void notifyParticipantsChanged() {
		Intent broadcast = new Intent();
		broadcast.setAction("jade.menu.REFRESH_PARTICIPANTS");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}

	private void notifySpoken(String speaker, String sentence) {
		Intent broadcast = new Intent();
		broadcast.setAction("jade.menu.REFRESH_CHAT_NEG");
		broadcast.putExtra("sentence", sentence + "\n");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}

	private class NegNotification extends OneShotBehaviour {
		private static final long serialVersionUID = -1426033904935339194L;
		private String sentence;

		private NegNotification(Agent a, String s) {
			super(a);
			sentence = s;
			//ultimoMensajeRecibido=s;
		}

		public void action() {
			spokenMsg.clearAllReceiver();
			Iterator it = participants.iterator();
			while (it.hasNext()) {
				spokenMsg.addReceiver((AID) it.next());
			}
			spokenMsg.setContent(sentence);
			notifySpoken(myAgent.getLocalName(), sentence);
			send(spokenMsg);
		}
	} // END of inner class NegNotification
	
	
	class ParticipantsManager extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		ParticipantsManager(Agent a) {
			super(a);
		}

		public void onStart() {
			ACLMessage subscription = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscription.setLanguage(codec.getName());
			subscription.setOntology(onto.getName());
			String convId = "C-" + myAgent.getLocalName();
			subscription.setConversationId(convId);
			subscription
					.addReceiver(new AID(NEG_MANAGER_NAME, AID.ISLOCALNAME));
			myAgent.send(subscription);
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						Predicate p = (Predicate) myAgent.getContentManager().extractContent(msg);
						if(p instanceof Joined) {
							Joined joined = (Joined) p;
							List<AID> aid = (List<AID>) joined.getWho();
							for(AID a : aid)
								participants.add(a);
							notifyParticipantsChanged();
						}
						if(p instanceof Left) {
							Left left = (Left) p;
							List<AID> aid = (List<AID>) left.getWho();
							for(AID a : aid)
								participants.remove(a);
							notifyParticipantsChanged();
						}
					} catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	} // END of inner class ParticipantsManager


	class NegListener extends CyclicBehaviour {
		private static final long serialVersionUID = 741233963737842521L;
		private MessageTemplate template = MessageTemplate
				.MatchConversationId(CHAT_NEG_ID);

		NegListener(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					notifySpoken(msg.getSender().getLocalName(),
							msg.getContent());
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	} // END of inner class NegListener
	
	
	private class Mensaje1a1 extends OneShotBehaviour {
		private static final long serialVersionUID = -1426033904935339194L;
		private String sentence;
		private AID dest;
		private Mensaje1a1(Agent a, String s, AID destinario) {
			super(a);
			sentence = s;
			dest=destinario;
			ultimoMensajeRecibido=s;
		}

		public void action() {
			spokenMsg.clearAllReceiver();
			spokenMsg.addReceiver(dest);
			spokenMsg.setContent(sentence);
			send(spokenMsg);
		}
	} // END of inner class Mensaje1a1
	
	
	private void handleUnexpected(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from "
					+ msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}


	@Override
	public String[] getParticipantNames() {
		// TODO Auto-generated method stub
		return null;
	}



	
}
