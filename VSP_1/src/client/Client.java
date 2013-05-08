
package client;


import java.net.UnknownHostException;

import lagern.Fach;
import lagern.Lager;
import lagern.LagerHelper;
import lagern.TFachlisteHolder;
import lagern.FachPackage.EInvalidCount;
import lagern.FachPackage.ENotEnoughPieces;
import lagern.LagerPackage.EAlreadyExists;
import lagern.LagerPackage.ENotFound;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;



/**
 * 
 * Verteilte Systeme Praktikum: "Aufgabe 1: Lager"
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 * Client Anwendung.
 * -----------------
 * 
 * client -ORBInitialPort <Port> -ORBInitialHost <IP> Lager { ende | liste |
 *   neu <FachName> | entferne <FachName> | einlagern <Fachname> <AnzahlTeile> |
 *   auslagern <FachName> <AnzahlTeile> | testloop }
 * 
 * 
 * @author Sebastian Mueller 2008588
 * @author Martin Schindler 2022759
 *
 */
public class Client {
	
	private final static String INIT_HOST = "-ORBInitialHost";
	private final static String INIT_PORT = "-ORBInitialPort";
	private final static String LAGERNAME = "Lager";
	private final static boolean DEBUG = false;
	
	static Lager lager;
	
	
	
	/**
	 * Client Anwendung.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
	    
	    String host;
	    String fachname;
	    String funktion;
	    int anzahl = -1;
	    int port = 0;
	    
	    if(DEBUG){
	    	for(int i = 0; i < args.length; i++){
	    		System.out.println("Argument " + i + " : " + args[i]);
	    	}
	    }
	    
	    if(args[5]==null)
	        usage();
	    
	    if(args[0].compareTo(INIT_HOST) != 0 || args[2].compareTo(INIT_PORT) !=0)
	        usage();
	    
	    try {
	        port = Integer.parseInt(args[3]);
	    } catch (NumberFormatException e) {
	        usage();
	    }//try
	    if(port < 1024 || port > 65535) {
	        System.err.println("Invalid Port Number.");
	        usage();
	    }//if
	    
	    host = args[1];
        try {
            java.net.InetAddress.getByName(host);
        } catch (UnknownHostException e1) {
            System.err.println("Invalid IP Address.");
        }//try
	    
		try {
		    // zugang zum namensdienst:
			ORB orb = ORB.init(args, null);
			// verbinden mit namensdienst:
			org.omg.CORBA.Object ogjRef = orb.resolve_initial_references("NameService");
			// CORBA object auf NamingContextExt casten:
			NamingContextExt ncRef = NamingContextExtHelper.narrow(ogjRef);
			lager = LagerHelper.narrow(ncRef.resolve_str(LAGERNAME));
			
			funktion = args[5];
			switch(funktion) {
			    case "ende":
			        quit();
		        break;
		        
			    case "liste":
			        liste();
			    break;
			    
			    case "neu":
			        if((fachname = args[6]) != null)
			            neu(fachname);
			        else
			            usage();
		        break;
		        
			    case "entferne":
			        if((fachname = args[6]) != null)
			            entferne(fachname);
	                else
	                    usage();
			    break;
			    
			    case "einlagern":
			        if((fachname = args[6]) != null && args[7] != null) {
			            try {
			                anzahl = Integer.parseInt(args[7]);
			            } catch (NumberFormatException e) {
			                usage();
			            }//try
			            if(anzahl != -1)
			                einlagern(fachname, anzahl);
			        } else
			            usage();
			    break;
			        
			    case "auslagern":
			        if((fachname = args[6]) != null && args[7] != null) {
			            try {
			                anzahl = Integer.parseInt(args[7]);
			            } catch (NumberFormatException e) {
			                usage();
			            }//try
			            if(anzahl != -1)
			                auslagern(fachname, anzahl);
			        } else
			            usage();
			    break;
			    
			    case "testloop":
			        if(args[6] != null)
			            try {
			                anzahl = Integer.parseInt(args[6]);
			            } catch (NumberFormatException e) {
			                usage();
			            }//try
			        testLoop(anzahl);
			    break;
			        
		        default:
		            usage();
		        break;
			}//switch
		} catch (InvalidName e) {
			System.err.println("Falscher Lagername.");
		} catch (NotFound e) {
		    System.err.println("Lager nicht Gefunden.");
		} catch (CannotProceed e) {
			System.err.println("Fehler bei der Ausfuehrung.");
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			System.err.println("Ungueltiger Lagername.");
		}//try
	}//main
	
	
	/**
	 * 
	 */
	private static void usage() {
        System.out.println("client -ORBInitialHost <IP> -ORBInitialPort <Port> Lager { ende | liste |");
        System.out.println(" neu <FachName> | entferne <FachName> | einlagern <Fachname> <AnzahlTeile> |");
        System.out.println(" auslagern <FachName> <AnzahlTeile> | testloop }\n");
        System.exit(0);
	}//usage
	
	
	/**
	 * Beenden des Lagers.
	 */
	private static void quit() {
	    lager.exit();
	    System.out.println("Lager beendet.");
	}//quit
	
	
	/**
	 * Ausgabe einer Liste aller Faecher und deren Anzahl an Teilen.
	 */
	private static void liste() {
		TFachlisteHolder faecher = new TFachlisteHolder();
	    int anzahlFaecher = lager.getFachliste(faecher);
	    System.out.printf("Anzahl Faecher: %d\n\n", anzahlFaecher);
	    for(Fach f: faecher.value)
	        System.out.printf("Fach: %s, Anzahl Teile: %d\n", f.name(), f.anzahl());
	    System.out.println();
	}//list
	
	/**
	 * Anlegen eines neuen Fachs.
	 * 
	 * @param name Name des Fachs.
	 */
	private static void neu(String name) {
	    try {
            lager.neu(name);
            System.out.printf("Fach \"%s\" angelegt.\n", name);
        } catch (EAlreadyExists e) {
            System.err.printf("Fach \"%s\" existiert bereits.\n", name);
        }//try
	}//create
	
	
	/**
	 * Entfernen eines Fachs.
	 * 
	 * @param name Name des Fachs.
	 */
	private static void entferne(String name) {
	    try {
            lager.loeschen(name);
            System.out.printf("Fach \"%s\" entfernt.\n", name);
        } catch (ENotFound e) {
            System.err.printf("Fach \"%s\" existiert nicht.\n", name);
        }//try
	}//delete
	
	
	/**
	 * Einlagern von Teilen in ein Fach.
	 * 
	 * @param name Name des Fachs.
	 * @param anzahl Anzahl Teile.
	 */
	private static void einlagern(String name, int anzahl) {
	    try {
            Fach fach = lager.hole(name);
            try {
                fach.einlagern(anzahl);
                System.out.printf("%d Teile in Fach \"%s\" eingelagert.\n", anzahl, name);

            } catch (EInvalidCount e) {
                System.err.printf("Ungueltige Anzahl: %d\n", anzahl);
                System.out.println(e.s);
            }//try
        } catch (ENotFound e) {
            System.err.printf("Fach \"%s\" existiert nicht.\n", name);
            System.out.println(e.s);
        }//try
	}//store
	
	
	/**
	 * Auslagern von Teilen aus einem Fach.
	 * 
	 * @param name Name des Fachs.
	 * @param anzahl Anzahl Teile.
	 */
	private static void auslagern(String name, int anzahl) {
	    try {
            Fach fach = lager.hole(name);
            try {
                fach.auslagern(anzahl);
                System.out.printf("%d Teile aus Fach \"%s\" ausgelagert.\n", anzahl, name);
            } catch (EInvalidCount e) {
                System.err.printf("Ungueltige Anzahl: %d\n", anzahl);
                System.out.println(e.s);
            } catch (ENotEnoughPieces e) {
                System.err.printf("Nicht genug Teile im Lager: %d\n", anzahl);
                System.out.println(e.s);
            }//try
        } catch (ENotFound e) {
            System.err.printf("Fach \"%s\" existiert nicht.\n", name);
            System.out.println(e.s);
        }//try
	}//release
	
	
	/**
	 * testLoop legt ein Fach an, holt sich eine Referenz darauf und fuehrt an-
	 * schließend eine Anzahl an Einlagerungen (mit je einem Teil) durch und
	 * ueberprueft anschliessend, ob die Anzahl der Teile mit der Anzahl der
	 * Einlagerungen uebereinstimmt.
	 * 
	 * @param anzahl Anzahl Einlagerungen.
	 */
	private static void testLoop(int anzahl) {
		String testfach = "testFach";
	    try {
            lager.neu(testfach);
        } catch (EAlreadyExists e1) {
            System.err.println("\"testFach\" existiert bereits.");
            return;
        }//try
	    
	    Fach fach = null;
        try {
            fach = lager.hole(testfach);
        } catch (ENotFound e) {
            System.err.println("\"testFach\" konnte nicht gefunden werden.");
            return;
        }//try
        
	    for(int i = 0 ; i < anzahl ; i++)
            try {
                fach.einlagern(1);
            } catch (EInvalidCount e) {
                System.err.println("Ungueltige Anzahl einzulagernder Teile");
                return;
            }//try
	    
	    if(fach.anzahl() == anzahl)
	        System.out.printf("%d Einlagerungen fehlerfrei vorgenommen.\n", anzahl);
	    else
	        System.err.printf("Ungueltige Anzahl Teile im Lager: soll: %d, ist: %d\n", anzahl, fach.anzahl());
	    
	    entferne(testfach);
	}//testLoop

}//Client
