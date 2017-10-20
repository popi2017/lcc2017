/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package agent;

import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
import android.content.Context;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetInitiator;

import java.util.Date;
import java.util.Vector;

public class ComensalAgent extends Agent implements ComensalAgentInterface {
	private ACLMessage spokenMsg;
	
	private static final long serialVersionUID = 1594371294421614291L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	private Context context;
	
	String [] nombreDelMenu ={"Vegetariano","de Parrilla","de Pizza","de Pasta"};
	
	private static final String CHAT_NEG_ID = "__chat__";
	private static final String NEG_MANAGER_NAME = "manager";

	private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology onto = ChatOntology.getInstance();
	
	
    //Número de ofertas que se considerarán.
    private int numeroDeOfertas;

    
    //Precio máximo que se pagará por el menu.
    private long precionMaximo=100000; 
    
    private int datoSobreMenu=999;
    private int datoSobreBarrio=999;
    private int datoSobreEstacionamiento=999;
    //private JSONObject obj = new JSONObject(); Ojo que si lo uso asi con mayuscula no puedo usar el parser
    
   
    private long tipoDePlato; 
    private long estacionamiento; 
    private long barrio; 
    private String restoPreferido=""; 
    
    private String tmpObjJson="";

    private JsonObject objetoJsonCfp = new JsonObject();
    
    JsonObject obj = new JsonObject();
    JsonParser parser = new JsonParser();
    
    
    private AID agenteNegociador = new AID();
  
 
//**
    
	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
			
			//AGREGO ESTO para tener el String de JSON que me envie como argumento en la pantalla MainActivity
			tmpObjJson=args[1].toString();
			obj = parser.parse(tmpObjJson).getAsJsonObject();

			precionMaximo=Integer.parseInt(obj.get("COTAMAXIMA").getAsString());			
			
			tipoDePlato=Integer.parseInt(obj.get("TIPOCOMIDA").getAsString());
			estacionamiento=Integer.parseInt(obj.get("ESTACIONAMIENTO").getAsString());
			barrio=Integer.parseInt(obj.get("BARRIO").getAsString());
			restoPreferido=obj.get("RESTO").getAsString();
			
			datoSobreMenu=Integer.parseInt(obj.get("DATOSOBREMENU").getAsString());
			datoSobreBarrio=Integer.parseInt(obj.get("DATOSOBREBARRIO").getAsString());
			datoSobreEstacionamiento=Integer.parseInt(obj.get("DATOSOBREESTACIONAMIENTO").getAsString());

			
			
			System.out.println("\n \n El Precio Maximo que estoy dispuesto a pagar es: "+precionMaximo);			
			System.out.println("\n \n El tipo de plato selecionado es: "+tipoDePlato);			
			System.out.println("\n \n Lo opcion referente al estacinamiento seleccionada es: "+estacionamiento);			
			System.out.println("\n \n El barrio donde deseo almorzar es: "+barrio);			
			System.out.println("\n \n Mi resto preferido es: "+restoPreferido);			

			System.out.println("\n \n El valor del Dato sobre compartir Menu es: "+datoSobreMenu);			
			System.out.println("\n \n El valor del Dato sobre compartir Barrio es: "+datoSobreBarrio);			
			System.out.println("\n \n El valor del Dato sobre compartir Estacionamiento es: "+datoSobreEstacionamiento);			

			
			System.out.printf("El comensale %s: a la espera de respuestas de restaurantes...\n", this.getLocalName());
			System.out.printf("\n\n Los argumentos que me envia como parametros de la otra pantalla: "+tmpObjJson+"\n\n", this.getLocalName());
		    
			//Registro del servicio de venta de menus en las páginas amarillas.
		        ServiceDescription servicio = new ServiceDescription();
		        servicio.setType("Comida");
		        servicio.setName("Menus Ejecutivos");
		 
		        DFAgentDescription descripcion = new DFAgentDescription();
		        descripcion.addLanguages("Español");
		        descripcion.addServices(servicio);
		 
		        try {
		              DFAgentDescription[] resultados = DFService.search(this, descripcion);
		                if (resultados.length <= 0) {
		                    System.out.println("No existen menus de ofertas disponibles.");
		        
		                } else {
		                    System.out.println("Busco menus de comida de ofertas, hay " + resultados.length + " ofertas de menu");
		           		 
		                    //Creamos el mensaje CFP(Call For Proposal) cumplimentando sus parámetros
		                    ACLMessage mensajeCFP = new ACLMessage(ACLMessage.CFP);
		                    for (DFAgentDescription agente:resultados) {
		                        mensajeCFP.addReceiver(agente.getName()); }
		                    
		                    //Protocolo que vamos a utilizar
		                    mensajeCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

		                   
		                    System.out.println("\n ENVIO AL AGENTE:  " + tmpObjJson + "\n");
		                    //System.out.println("\n UN SOLO ELEMENTO ES por ejemplo:  " + obj.get("COTAMAXIMA").getAsString() + "\n");
		                    
		                    if(datoSobreMenu>0){
		                    	objetoJsonCfp.addProperty("TIPOCOMIDA",tipoDePlato+"");
		                    }
		                    else {
		                    	objetoJsonCfp.addProperty("TIPOCOMIDA","0");
		                    }

		                    if(datoSobreBarrio>0){
		                    	objetoJsonCfp.addProperty("BARRIO",barrio+"");
		                    }
		                    else {
		                    	objetoJsonCfp.addProperty("BARRIO","0");
		                    }

		                    
		                    if(datoSobreEstacionamiento>0){
		                    	objetoJsonCfp.addProperty("ESTACIONAMIENTO",estacionamiento+"");
		                    }
		                    else {
		                    	objetoJsonCfp.addProperty("ESTACIONAMIENTO","0");
		                    }

		                    String cfpData=objetoJsonCfp.toString();

		                    mensajeCFP.setContent(cfpData);
		                    
		 
		                    //Indicamos el tiempo que esperaremos por las ofertas.
		                    //EL QUE HAbiLITE ABAJO ES EL NUEVO TIEMPO DE ESPERA PARA QUE NO DEMORE TANTO
		                    //mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + 15000));
							
		                    mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + 40000));
		                    //Se añade el comportamiento que manejará las ofertas.
		                    this.addBehaviour(new ManejoOpciones(this, mensajeCFP));
		                	}
		        } catch (Exception e) {
	                e.printStackTrace();
	            }
				// Register language and ontology
				ContentManager cm = getContentManager();
				cm.registerLanguage(codec);
				cm.registerOntology(onto);
				cm.setValidationMode(false);

				// Add initial behaviours
				addBehaviour(new ParticipantsManager(this));
				addBehaviour(new NegListener(this));

				spokenMsg = new ACLMessage(ACLMessage.INFORM);
				spokenMsg.setConversationId(CHAT_NEG_ID);

				// Activate the GUI
				registerO2AInterface(ComensalAgentInterface.class, this);
				
				Intent broadcast = new Intent();
				broadcast.setAction("jade.menu.SHOW_CHAT_NEG");
				logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
				context.sendBroadcast(broadcast);
			}		
	}//FIN DEL SETUP
		    
	   private class ManejoOpciones extends ContractNetInitiator {
		   
	        public ManejoOpciones(Agent agente, ACLMessage plantilla) {
	            super(agente, plantilla);
	        }
	 
	        //Manejador de proposiciones.
	        protected void handlePropose(ACLMessage propuesta, Vector aceptadas) {
	            System.out.printf("%s: Recibi oferta del Restaurant %s. Ofrece un menu por %s pesos.\n",
	                this.myAgent.getLocalName(), propuesta.getSender().getLocalName(), propuesta.getContent());
	            	
				JsonObject objRecibido = new JsonObject();
				objRecibido = parser.parse(propuesta.getContent().toString()).getAsJsonObject();
				int tmpPrecioCartel= objRecibido.get("propuesta_precio").getAsInt();
				int tmpEstacionamiento= objRecibido.get("propuesta_estacionamiento").getAsInt();
				
				String infoEstacionamientoCartel="";
				if (tmpEstacionamiento>1) {
							infoEstacionamientoCartel="Gratuito";
				}
				
				else{
					infoEstacionamientoCartel="Pago";
				}
				
	            addBehaviour(new Mensaje1a1(this.myAgent, "\n\nEl resto: "+propuesta.getSender().getLocalName()+"ofrece un menu "+nombreDelMenu[(int) (tipoDePlato-1)]+" por $"+tmpPrecioCartel+". En relación al estacionamiento es "+infoEstacionamientoCartel+"\n\n",this.myAgent.getAID()));

	            addBehaviour(new Mensaje1a1(this.myAgent, "\n Se ofrece al comensal "+ myAgent.getLocalName() +" un menu "+nombreDelMenu[(int) (tipoDePlato-1)]+" por $"+tmpPrecioCartel+". En relación al estacionamiento es "+infoEstacionamientoCartel+"\n\n",propuesta.getSender()));

	        }
	 
	        //Manejador de rechazos de proposiciones.
	        protected void handleRefuse(ACLMessage rechazo) {
	            System.out.printf("%s: Restaurante %s no tiene ningún menu compatible con mis preferencias para ofrecer en este momento.\n",
	                this.myAgent.getLocalName(), rechazo.getSender().getLocalName());
	            notifySpoken(this.myAgent.getLocalName(),"Restaurante "+ rechazo.getSender().getLocalName() + " no tiene ningún menu compatible con mis preferencias para ofrecer en este momento. ");

	        }
	 
	        //Manejador de respuestas de fallo.
	        protected void handleFailure(ACLMessage fallo) {
	            if (fallo.getSender().equals(myAgent.getAMS())) {
	 
	        //Esta notificacion viene del entorno de ejecución JADE (no existe el receptor)
	                System.out.println("AMS: Este menu de oferta no existe mas o no es accesible por el momento. ");

	            } else {
	                System.out.printf("%s: Restaurante %s ha sufrido un fallo.\n",
	                    this.myAgent.getLocalName(), fallo.getSender().getLocalName());
	                notifySpoken(this.myAgent.getLocalName(),"Restaurante: " + fallo.getSender().getLocalName() + " ha sufrido un fallo");

	            }
	            //Falló, por lo tanto, no recibiremos respuesta desde ese agente
	            ComensalAgent.this.numeroDeOfertas--;
	        }
	  
	        
	        //Método colectivo llamado tras finalizar el tiempo de espera o recibir todas las propuestas.
	        protected void handleAllResponses(Vector respuestas, Vector aceptados) {
	 
	        //Se comprueba si una oferta de menu se pasó del plazo de envío de ofertas.
	            if (respuestas.size() < numeroDeOfertas) {
	                System.out.printf("%s: %d son las ofertas de menu que llegan tarde.\n",
	                    this.myAgent.getLocalName(), ComensalAgent.this.numeroDeOfertas - respuestas.size());
	                notifySpoken(this.myAgent.getLocalName(),"Las ofertas de menu que llegaron tarde fueron"+ (ComensalAgent.this.numeroDeOfertas - respuestas.size() + "\n \n \n"));
	                addBehaviour(new NegNotification(this.myAgent, "\n \n Disculpe pero su respuesta llego demasiado TARDE!!"));
	            }
	 
	            //Escogemos la mejor oferta
	            int mejorOferta = Integer.MAX_VALUE;
	            AID mejorResto = null;
	            ACLMessage aceptado = null;
	            for (Object resp:respuestas) {
	                ACLMessage mensaje = (ACLMessage) resp;
	                if (mensaje.getPerformative() == ACLMessage.PROPOSE) {
	                    ACLMessage respuesta = mensaje.createReply();
	                    respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
	                    aceptados.add(respuesta);
	 
	                    //Si la oferta es la mejor (inferior a todas las otras)
	                    //Se almacena su precio y el AID del resto que hizo la mejor propuesta por el momento.
	                    //int oferta = Integer.parseInt(mensaje.getContent());
	                    JsonObject tmpObj= new JsonObject();
	        			tmpObj = parser.parse(mensaje.getContent()).getAsJsonObject();
	        			int oferta = Integer.parseInt(tmpObj.get("propuesta_precio").getAsString());
	        			int estac =  Integer.parseInt(tmpObj.get("propuesta_estacionamiento").getAsString());
	                    if (oferta <= precionMaximo && oferta <= mejorOferta && estac>=estacionamiento) {
	                        mejorOferta = oferta;
	                        mejorResto = mensaje.getSender();
	                        aceptado = respuesta;
	                    }
	                }
	            }
	 
	            //Si hay una oferta aceptada se modifica su performativa, es decir en principio pongo todos como rechazados, y despues al que hizo la mejor oferta que sea compatible con mis gustos lo pongo en aceptado.
	            if (aceptado != null) {
	                System.out.printf("%s: Decidido!!! Reserveme un menu por favor %s\n",
	                    this.myAgent.getLocalName(), mejorResto.getLocalName());
	                
	                addBehaviour(new Mensaje1a1(this.myAgent,"RW_ACEPTADO-- Decidido!!! Reserveme un menu por favor Restaurante "+mejorResto.getLocalName()+ "\n \n \n",this.myAgent.getAID()));
	                addBehaviour(new Mensaje1a1(this.myAgent,"RW_ACEPTADO-- Decidido!!! Reserveme un menu por favor Restaurante "+mejorResto.getLocalName()+ "\n \n \n",mejorResto));
	                
	                aceptado.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
	            }
	            
	            if (aceptado==null){
	            	System.out.printf("\n NINGUNA PROPUESTA ME CONVENCIO\n");
	            }
	        }
	        
	        //Manejador de los mensajes inform.
	        protected void handleInform(ACLMessage inform) {
	            System.out.printf("%s: Restaurante %s te ha reservado un lugar.\n",
	                this.myAgent.getLocalName(), inform.getSender().getName());
                //addBehaviour(new NegNotification(this.myAgent, "\n \n SE HA RECIBIDO CONFIMRACION DE RESERVA!!!! \n\n"));

	        }

	
	   }
	
	
	
	// ///////////////////////////////////////
	// Methods called by the interface
	// ///////////////////////////////////////
	
	
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
	/**
	 * Inner class NegNotification.
	 */
	private class NegNotification extends OneShotBehaviour {
		private static final long serialVersionUID = -1426033904935339194L;
		private String sentence;

		private NegNotification(Agent a, String s) {
			super(a);
			sentence = s;
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
	

	private class Mensaje1a1 extends OneShotBehaviour {
		private static final long serialVersionUID = -1426033904935339194L;
		private String sentence;
		private AID dest;
		
		private Mensaje1a1(Agent a, String s, AID destinario) {
			super(a);
			sentence = s;
			dest=destinario;
		}

		public void action() {
			spokenMsg.clearAllReceiver();
			spokenMsg.addReceiver(dest);
			spokenMsg.setContent(sentence);
			send(spokenMsg);
		}
	} // END of inner class Mensaje1a1
	
	
	private class infoExtra extends OneShotBehaviour {
		private static final long serialVersionUID = -1426033904935339194L;
		private String sentence;
		private AID dest;
		private infoExtra(Agent a, String s, AID destinario) {
			super(a);
			dest=destinario;
			sentence=s;
		}

		public void action() {
			spokenMsg.clearAllReceiver();
			spokenMsg.addReceiver(dest);
			spokenMsg.setContent(sentence);
			send(spokenMsg);
		}
	} // END of inner class infoExtra
	
	
	//Inner class ParticipantsManager
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
							//notifyParticipantsChanged(); COMENTAR o DESCOMENTAR
						}
						if(p instanceof Left) {
							Left left = (Left) p;
							List<AID> aid = (List<AID>) left.getWho();
							for(AID a : aid)
								participants.remove(a);
							//notifyParticipantsChanged(); COMENTAR o DESCOMENTAR
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

	
	//Inner class NegListener. 
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
	
	// ///////////////////////////////////////
	// Private utility method
	// ///////////////////////////////////////
	private void handleUnexpected(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from "
					+ msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}
	


	   public void handleSpoken(String s) {
			addBehaviour(new NegNotification(this, s));
		}


	   public void autoMensaje(String s) {
			addBehaviour(new Mensaje1a1(this, s,this.getAID()));
		}
	   
	   public void masDatos(String s) {
			addBehaviour(new infoExtra(this,s,agenteNegociador));
		}
}
