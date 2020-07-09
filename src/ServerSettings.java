import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class ServerSettings implements Serializable {
    private static final long serialVersionUID = 1;
    private Guild server;
    private char prefix;
    private HashMap<Role, Permissions> newPollPerms;
    private HashMap<Role, Permissions> reactPerms;
    private HashMap<Role, Permissions> prefixPerms;

    public ServerSettings(Guild server){
        if(server != null){
            this.server = server;
            this.prefix = '-';
            this.newPollPerms = new HashMap<Role, Permissions>();
            this.reactPerms = new HashMap<Role, Permissions>();
            this.prefixPerms = new HashMap<Role, Permissions>();
            this.refresh();            
        } else{
            System.out.println("Erreur ServerSettings(): parametre non valide");
        }
    }

    public void refresh(){
        Set<Role> listedRoles = this.newPollPerms.keySet();
        // Adds the new roles
        for(Role role : this.server.getRoles()){
            if(!listedRoles.contains(role)){
                if(role.isPublicRole()){
                    this.newPollPerms.put(role, Permissions.ALLOWED);
                    this.reactPerms.put(role, Permissions.ALLOWED);
                    this.prefixPerms.put(role, Permissions.ALLOWED);
                } else{
                    this.newPollPerms.put(role, Permissions.DENIED);
                    this.reactPerms.put(role, Permissions.DENIED);
                    this.prefixPerms.put(role, Permissions.DENIED);
                }
            }
        }
        List<Role> serverRoles = this.server.getRoles();
        // Remove the old roles
        for(Role role : this.newPollPerms.keySet()){
            if(!serverRoles.contains(role)){
                this.newPollPerms.remove(role);
                this.reactPerms.remove(role);
            }
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
    
    public Permissions getPrefixPermission(Role role){
        Permissions ret = null;
        if(role != null){
            ret = this.prefixPerms.get(role);
        } else{
            System.out.println("Erreur ServerSettings.getPrefixPermission(): parametre non valide");
        }
        return ret;
    }

    public void setPrefixPermission(Role role, Permissions perm){
        if(role != null && perm != null){
            this.prefixPerms.remove(role);
            this.prefixPerms.put(role, perm);
        } else{
            System.out.println("Erreur ServerSettings.setPrefixPermission(): parametre non valide");
        }
    }
}