import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

// TODO - add custom emotes for 11 to 30 scores
// TODO - save prefix and permissions
public class EventManager extends ListenerAdapter {

    private SelfUser bot;
    private HashMap<Guild, ServerSettings> guildsSets;

    public EventManager(){
        this.bot = null;
        this.guildsSets = null;
    }

    public void onGuildJoin(GuildJoinEvent event){
        this.guildsSets.put(event.getGuild(), new ServerSettings(event.getGuild()));
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        Member user = event.getMember();
        MessageChannel chann = event.getChannel();
        Message msg = event.getMessage();
        if(!msg.getAuthor().isBot()){
            String msgTxt = msg.getContentRaw();
            String[] args = msgTxt.split(" ");
            if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"moy")){
                if(this.hasPollPerms(event.getGuild(), user.getRoles())){
                    args = msgTxt.split("\"");
                    if(args.length == 3){
                        try {
                            // Used to test if the second arg is a number
                            int maxScore = Integer.parseInt(args[2].trim());

                            chann.sendMessage(args[1]).append("\nMoyenne : 0/"+maxScore)
                            .queue(message -> {if(maxScore <= 10){
                                                    for(int i=0; i < maxScore; i++){
                                                        message.addReaction("U+003"+String.valueOf(i)+" U+FE0F U+20E3").queue();
                                                    }
                                                    if(maxScore == 10){
                                                        message.addReaction("U+1F51F").queue();
                                                    } else{
                                                        message.addReaction("U+003"+maxScore+" U+FE0F U+20E3").queue();
                                                    }
                                                }});
                        } catch (NumberFormatException e) {
                            chann.sendMessage(args[1]).append("\nMoyenne : ?").queue();
                        }
                    } else{
                        chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"moy \"MESSAGE\" NOTE_MAX").queue();
                    }
                } else{
                    chann.sendMessage(user.getAsMention()+" Vous n'avez pas la permission de créer un sondage").queue();
                }
            } else if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"prefix")){
                if(this.hasPrefixPerms(event.getGuild(), user.getRoles())){
                    if(args.length == 2 && args[1].length() == 1){
                        char oldPref = this.guildsSets.get(event.getGuild()).getPrefix();
                        char newPref = args[1].charAt(0);
                        this.guildsSets.get(event.getGuild()).setPrefix(newPref);
                        chann.sendMessage("Le préfixe des commandes a été changé de \""+oldPref+"\" à \""+newPref+"\"").queue();
                    } else{
                        chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"prefix CARACTERE").queue();
                    }
                } else{
                    chann.sendMessage(user.getAsMention()+" Vous n'avez pas la permission de modifier le préfixe des commandes").queue();
                }
            } else if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"perms")){
                if(args.length == 1){
                    String listPerms = "";
                    String rName;
                    for(Role r : event.getGuild().getRoles()){
                        if(!r.isManaged()){
                            rName = r.getName();
                            if(rName.length() > 1 && rName.charAt(0) == '@'){
                                rName = rName.substring(1);
                            }
                            listPerms = listPerms + "• "+rName+"\n";
                            if(this.guildsSets.get(event.getGuild()).getReactPermission(r) == Permissions.ALLOWED){
                                listPerms = listPerms + "React : \u2705\n";
                            } else{
                                listPerms = listPerms + "React : \u274C\n";
                            }
                            if(this.guildsSets.get(event.getGuild()).getPollPermission(r) == Permissions.ALLOWED){
                                listPerms = listPerms + "Poll : \u2705\n";
                            } else{
                                listPerms = listPerms + "Poll : \u274C\n";
                            }
                            if(this.guildsSets.get(event.getGuild()).getPrefixPermission(r) == Permissions.ALLOWED){
                                listPerms = listPerms + "Prefix : \u2705\n";
                            } else{
                                listPerms = listPerms + "Prefix : \u274C\n";
                            }
                            if(r.hasPermission(Permission.MANAGE_PERMISSIONS)){
                                listPerms = listPerms + "Manage perms : \u2705\n";
                            } else{
                                listPerms = listPerms + "Manage perms : \u274C\n";
                            }
                        }
                    }
                    chann.sendMessage(listPerms).queue();
                } else if(args.length >= 4){
                    if(user.hasPermission(Permission.MANAGE_PERMISSIONS)){
                        List<Role> mRoles = msg.getMentionedRoles();
                        try{
                            Permissions newPerm = Permissions.valueOf(args[args.length-1]);
                            // Set perms for everyone role if mentioned
                            if(msg.mentionsEveryone()){
                                List<Role> serverRoles = event.getGuild().getRoles();
                                boolean found = false;
                                int i = 0;
                                while(i < serverRoles.size() && !found){
                                    if(serverRoles.get(i).isPublicRole()){
                                        found = true;
                                        if(args[1].equals("poll")){
                                            this.guildsSets.get(event.getGuild()).setPollPermission(serverRoles.get(i), newPerm);
                                        } else if(args[1].equals("react")){
                                            this.guildsSets.get(event.getGuild()).setReactPermission(serverRoles.get(i), newPerm);
                                        } else if(args[1].equals("prefix")){
                                            this.guildsSets.get(event.getGuild()).setPrefixPermission(serverRoles.get(i), newPerm);                           
                                        }
                                    }
                                    i++;
                                }
                            }
                            if(args[1].equals("poll")){
                                for(Role r : mRoles){
                                    this.guildsSets.get(event.getGuild()).setPollPermission(r, newPerm);
                                }
                            } else if(args[1].equals("react")){
                                for(Role r : mRoles){
                                    this.guildsSets.get(event.getGuild()).setReactPermission(r, newPerm);
                                }
                            } else if(args[1].equals("prefix")){
                                for (Role r : mRoles) {
                                    this.guildsSets.get(event.getGuild()).setPrefixPermission(r, newPerm);
                                }
                            } else{
                                chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll|prefix role [roles additionnels...] ALLOWED|DENIED]").queue();
                            }
                            chann.sendMessage("Les nouvelles permissions sont appliquées").queue();
                        } catch(IllegalArgumentException e){
                            chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll|prefix role [roles additionnels...] ALLOWED|DENIED]").queue();
                        }
                    } else{
                        chann.sendMessage(user.getAsMention()+" Vous n'avez pas la permission de modifier les permissions :upside_down:").queue();
                    }
                } else{
                    chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll|prefix role [roles additionnels...] ALLOWED|DENIED]").queue();
                }
            }
        }
    }

    public void buildEmbedMoyMessage(String title, String text, String footer){
        
    }

    public void onMessageReactionAdd(MessageReactionAddEvent event){
        event.retrieveMessage().queue(message -> {
            if(message.getAuthor().equals(this.bot)){
                Member user = event.getMember();
                if(this.hasReactPerms(event.getGuild(), user.getRoles())){
                    int maxScore = Integer.parseInt(message.getContentRaw().split("/")[1]);
                    double sumScore = 0;
                    int nbMarks = 0;
                    double avg = 0;
                    List<MessageReaction> l = message.getReactions();
                    if(l.size() > maxScore){
                        for(int i=0; i <= maxScore; i++){
                            sumScore = sumScore + (i*(l.get(i).getCount()-1));
                            nbMarks = nbMarks + l.get(i).getCount()-1;
                        }
                        if(nbMarks > 0){
                            avg = sumScore/nbMarks;
                        }
                    }
                    String txt = message.getContentRaw().split("\n")[0];
                    message.editMessage(txt+"\nMoyenne : "+avg+"/"+maxScore).queue();
                } else{
                    RestAction<PrivateChannel> privchann = user.getUser().openPrivateChannel();
                    privchann.queue(privChann -> privChann.sendMessage("Vous n'avez pas la permission de poster une réaction au message se trouvant dans \""+event.getGuild().getName()+"\"").queue());
                    event.getReaction().removeReaction(event.getUser()).queue();
                }
            }
        });
    }

    public void onMessageReactionRemove(MessageReactionRemoveEvent event){
        event.retrieveMessage().queue(message -> {
            if(message.getAuthor().equals(this.bot)){
                int maxScore = Integer.parseInt(message.getContentRaw().split("/")[1]);
                double sumScore = 0;
                int nbMarks = 0;
                double avg = 0;
                List<MessageReaction> l = message.getReactions();
                if(l.size() > maxScore){
                    for(int i=0; i <= maxScore; i++){
                        sumScore = sumScore + (i*(l.get(i).getCount()-1));
                        nbMarks = nbMarks + l.get(i).getCount()-1;
                    }
                    if(nbMarks > 0){
                        avg = sumScore/nbMarks;
                    }
                }
                String txt = message.getContentRaw().split("\n")[0];
                message.editMessage(txt+"\nMoyenne : "+avg+"/"+maxScore).queue();
            }
        });
    }

    public void onRoleCreate(RoleCreateEvent event){
        this.guildsSets.get(event.getGuild()).refresh();
    }

    public void onRoleDelete(RoleDeleteEvent event){
        this.guildsSets.get(event.getGuild()).refresh();
    }

    public boolean hasPollPerms(Guild g, List<Role> r){
        boolean ret = false;
        if(g != null && r != null){
            // Checks everyone permission
            List<Role> serverRoles = g.getRoles();
            boolean found = false;
            int i = 0;
            while(i < serverRoles.size() && !found){
                if(serverRoles.get(i).isPublicRole()){
                    found = true;
                    ret = (this.guildsSets.get(g).getPollPermission(serverRoles.get(i)) == Permissions.ALLOWED);
                }
                i++;
            }
            // Check user roles permissions
            i = 0;
            while(i < r.size() && !ret){
                if(this.guildsSets.get(g).getPollPermission(r.get(i)) == Permissions.ALLOWED){
                    ret = true;
                }
                i++;
            }
        } else{
            System.out.println("Erreur EventManager.hasPollPerms(): parametre non valide");
        }
        return ret;
    }

    public boolean hasReactPerms(Guild g, List<Role> r){
        boolean ret = false;
        if(g != null && r != null){
            // Checks everyone permission
            List<Role> serverRoles = g.getRoles();
            boolean found = false;
            int i = 0;
            while(i < serverRoles.size() && !found){
                if(serverRoles.get(i).isPublicRole()){
                    found = true;
                    ret = (this.guildsSets.get(g).getReactPermission(serverRoles.get(i)) == Permissions.ALLOWED);
                }
                i++;
            }
            // Check user roles permissions
            i = 0;
            while(i < r.size() && !ret){
                if(this.guildsSets.get(g).getReactPermission(r.get(i)) == Permissions.ALLOWED){
                    ret = true;
                }
                i++;
            }
        } else{
            System.out.println("Erreur EventManager.hasReactPerms(): parametre non valide");
        }
        return ret;
    }

    public boolean hasPrefixPerms(Guild g, List<Role> r){
        boolean ret = false;
        if(g != null && r != null){
            // Checks everyone permission
            List<Role> serverRoles = g.getRoles();
            boolean found = false;
            int i = 0;
            while(i < serverRoles.size() && !found){
                if(serverRoles.get(i).isPublicRole()){
                    found = true;
                    ret = (this.guildsSets.get(g).getPrefixPermission(serverRoles.get(i)) == Permissions.ALLOWED);
                }
                i++;
            }
            // Check user roles permissions
            i = 0;
            while(i < r.size() && !ret){
                if(this.guildsSets.get(g).getPrefixPermission(r.get(i)) == Permissions.ALLOWED){
                    ret = true;
                }
                i++;
            }
        } else{
            System.out.println("Erreur EventManager.hasPrefixPerms(): parametre non valide");
        }
        return ret;
    }

    public void setUser(SelfUser user){
        if(user != null){
            this.bot = user;
            this.guildsSets = new HashMap<Guild, ServerSettings>();
            for(Guild guild : this.bot.getJDA().getGuilds()){
                this.guildsSets.put(guild, new ServerSettings(guild));
            }
        } else{
            System.out.println("Erreur EventManager.setUser(): parametre non valide");
        }
    }
}