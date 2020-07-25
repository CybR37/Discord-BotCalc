import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Set;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class ServerSettings {
    private Guild server;
    private char prefix;
    private HashMap<Role, Permissions> newPollPerms;
    private HashMap<Role, Permissions> reactPerms;
    private HashMap<Role, Permissions> prefixPerms;

    public ServerSettings(Guild server){
        if(server != null){
            this.prefix = '-';
            this.server = server;
            this.newPollPerms = new HashMap<Role, Permissions>();
            this.reactPerms = new HashMap<Role, Permissions>();
            this.prefixPerms = new HashMap<Role, Permissions>();
            for (Role r : this.server.getRoles()){
                this.addRole(r);
            }
        } else{
            System.out.println("Erreur ServerSettings(): parametre non valide");
        }
    }

    public void addRole(Role r){
        if(r.isPublicRole()){
            this.newPollPerms.put(r, Permissions.ALLOWED);
            this.reactPerms.put(r, Permissions.ALLOWED);
            this.prefixPerms.put(r, Permissions.ALLOWED);
        } else{
            this.newPollPerms.put(r, Permissions.DENIED);
            this.reactPerms.put(r, Permissions.DENIED);
            this.prefixPerms.put(r, Permissions.DENIED);
        }
    }

    public void removeRole(Role r){
        this.newPollPerms.remove(r);
        this.reactPerms.remove(r);
        this.prefixPerms.remove(r);
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

    public void saveSettings(String fileName){
        File saveFile = new File("sets/"+fileName);
        saveFile.getParentFile().mkdir();
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(saveFile));
            out.writeChar(this.prefix);
            Set<Role> listR = this.newPollPerms.keySet();
            for (Role role : listR) {
                out.writeLong(role.getIdLong());
                out.writeUTF(this.newPollPerms.get(role).toString());
                out.writeUTF(this.reactPerms.get(role).toString());
                out.writeUTF(this.prefixPerms.get(role).toString());
            }
            out.close();
        } catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public void loadSettings(String fileName){
        File saveFile = new File("sets/"+fileName);
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(saveFile));
            try{
                this.prefix = in.readChar();
                while(true){
                    long roleId = in.readLong();
                    this.setPollPermission(this.server.getRoleById(roleId), Permissions.valueOf(in.readUTF()));
                    this.setReactPermission(this.server.getRoleById(roleId), Permissions.valueOf(in.readUTF()));
                    this.setPrefixPermission(this.server.getRoleById(roleId), Permissions.valueOf(in.readUTF()));
                }
            } catch(java.io.EOFException e){
            } finally{
                in.close();
            }
        } catch(java.io.IOException e){
            e.printStackTrace();
        }
    }
}