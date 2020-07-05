import java.io.Serializable;
import java.util.HashMap;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class ServerSettings implements Serializable {
    private static final long serialVersionUID = 1;
    private char prefix;
    private HashMap<Role, Permissions> newPollPerms;
    private HashMap<Role, Permissions> reactPerms;

    public ServerSettings(Guild server){
        if(server != null){
            this.prefix = '-';
            this.newPollPerms = new HashMap<Role, Permissions>();
            this.reactPerms = new HashMap<Role, Permissions>();
            for(Role role : server.getRoles()){
                this.newPollPerms.put(role, Permissions.ALLOWED);
                this.reactPerms.put(role, Permissions.ALLOWED);
            }
        } else{
            System.out.println("Erreur ServerSettings(): parametre non valide");
        }
    }

    public char getPrefix(){
        return this.prefix;
    }

    public void setPrefix(char pref){
        this.prefix = pref;
    }

    public Permissions getPollPermission(Role role){
        Permissions ret = null;
        if(role != null){
            ret = this.newPollPerms.get(role);
        } else{
            System.out.println("Erreur ServerSettings.getPollPermission(): parametre non valide");
        }
        return ret;
    }

    public void setPollPermission(Role role, Permissions perm){
        if(role != null && perm != null){
            this.newPollPerms.remove(role);
            this.newPollPerms.put(role, perm);
        } else{
            System.out.println("Erreur ServerSettings.setPollPermission(): parametre non valide");
        }
    }

    public Permissions getReactPermission(Role role){
        Permissions ret = null;
        if(role != null){
            ret = this.reactPerms.get(role);
        } else{
            System.out.println("Erreur ServerSettings.getReactPermission(): parametre non valide");
        }
        return ret;
    }

    public void setReactPermission(Role role, Permissions perm){
        if(role != null && perm != null){
            this.reactPerms.remove(role);
            this.reactPerms.put(role, perm);
        } else{
            System.out.println("Erreur ServerSettings.setReactPermission(): parametre non valide");
        }
    }
}