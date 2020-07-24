import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
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
import net.dv8tion.jda.api.requests.restaction.MessageAction;

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

    public void onGuildLeave(GuildLeaveEvent event){
        this.guildsSets.remove(event.getGuild());
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
                            if(maxScore <= 20){
                                List<Emote> serverEmotes = event.getGuild().getEmotes();
                                String txtEmote = null;
                                String[] unicodeList = {"🤏", "✌️", "🤞", "🤙", "👈", "👉", "👆", "👇", "👍", "👏"};
                                MessageAction sentMsg = chann.sendMessage(args[1]).append("\nMoyenne : 0/"+maxScore);
                                if(maxScore > 10){
                                    sentMsg.append("\n(");
                                    if(serverEmotes.size() >= maxScore-10){
                                        for(int i=0; i < maxScore-10; i++){
                                            txtEmote = (i+11)+" -> "+serverEmotes.get(i).getAsMention();
                                            if(i == 0){
                                                sentMsg.append(txtEmote);
                                            } else{
                                                sentMsg.append(" | "+txtEmote);
                                            }
                                        }
                                    } else{
                                        for(int i=0; i < maxScore-10; i++){
                                            txtEmote = (i+11)+" -> "+unicodeList[i];
                                            if(i == 0){
                                                sentMsg.append(txtEmote);
                                            } else{
                                                sentMsg.append(" | "+txtEmote);
                                            }
                                        }
                                    }
                                    sentMsg.append(")");
                                }
                                sentMsg.queue(message -> {if(maxScore <= 10){
                                                            for(int i=0; i < maxScore; i++){
                                                                message.addReaction("U+003"+String.valueOf(i)+" U+FE0F U+20E3").queue();
                                                            }
                                                            if(maxScore == 10){
                                                                message.addReaction("U+1F51F").queue();
                                                            } else{
                                                                message.addReaction("U+003"+maxScore+" U+FE0F U+20E3").queue();
                                                            }
                                                        } else{
                                                            // Add 0 emoji as reaction
                                                            if(maxScore < 20){
                                                                message.addReaction("U+0030 U+FE0F U+20E3").queue();
                                                            }
                                                            // Add 1-9 emoji as reaction
                                                            for(int j=1; j < 10; j++){
                                                                message.addReaction("U+003"+String.valueOf(j)+" U+FE0F U+20E3").queue();
                                                            }
                                                            // Add 10 emoji as reaction
                                                            message.addReaction("U+1F51F").queue();
                                                            List<Emote> messageEmotes = message.getEmotes();
                                                            if(messageEmotes.size() > maxScore-10){
                                                                // Keep only marks emotes
                                                                messageEmotes = messageEmotes.subList(messageEmotes.size()-(maxScore-10), messageEmotes.size());
                                                            }
                                                            // 11-maxScore (limited to 20) emoji as reaction
                                                            if(messageEmotes.size() != maxScore-10){
                                                                for(int i=0; i < maxScore-10; i++){
                                                                    message.addReaction(unicodeList[i]).queue();
                                                                }
                                                            } else{
                                                                for(Emote emo : messageEmotes){
                                                                    message.addReaction(emo).queue();
                                                                }
                                                            }
                                                        }});
                            } else{
                                chann.sendMessage("La note maximale ne peut dépasser 20").queue();
                            }
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
                    String[] lines = message.getContentRaw().split("\n");
                    int maxScore = Integer.parseInt(lines[1].split("/")[1]);
                    double sumScore = 0;
                    int nbMarks = 0;
                    double avg = 0;
                    List<MessageReaction> l = message.getReactions();
                    if(l.size() > maxScore){
                        for(int i=0; i <= maxScore; i++){
                            sumScore = sumScore + (i*(l.get(i).getCount()-1));
                            nbMarks = nbMarks + l.get(i).getCount()-1;
                        }
                    } else if(l.size() >= maxScore && maxScore == 20){
                        for(int i=0; i < maxScore; i++){
                            sumScore = sumScore + ((i+1)*(l.get(i).getCount()-1));
                            nbMarks = nbMarks + l.get(i).getCount()-1;
                        }
                    }
                    if(nbMarks > 0){
                        avg = sumScore/nbMarks;
                    }
                    if(lines.length > 2){
                        message.editMessage(lines[0]+"\nMoyenne : "+avg+"/"+maxScore+"\n"+lines[2]).queue();
                    } else{
                        message.editMessage(lines[0]+"\nMoyenne : "+avg+"/"+maxScore).queue();
                    }
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
                String[] lines = message.getContentRaw().split("\n");
                int maxScore = Integer.parseInt(lines[1].split("/")[1]);
                double sumScore = 0;
                int nbMarks = 0;
                double avg = 0;
                List<MessageReaction> l = message.getReactions();
                if(l.size() > maxScore){
                    for(int i=0; i <= maxScore; i++){
                        sumScore = sumScore + (i*(l.get(i).getCount()-1));
                        nbMarks = nbMarks + l.get(i).getCount()-1;
                    }
                } else if(l.size() >= maxScore && maxScore == 20){
                    for(int i=0; i < maxScore; i++){
                        sumScore = sumScore + ((i+1)*(l.get(i).getCount()-1));
                        nbMarks = nbMarks + l.get(i).getCount()-1;
                    }
                }
                if(nbMarks > 0){
                    avg = sumScore/nbMarks;
                }
                if(lines.length > 2){
                    message.editMessage(lines[0]+"\nMoyenne : "+avg+"/"+maxScore+"\n"+lines[2]).queue();
                } else{
                    message.editMessage(lines[0]+"\nMoyenne : "+avg+"/"+maxScore).queue();
                }
            }
        });
    }

    public void onRoleCreate(RoleCreateEvent event){
        this.guildsSets.get(event.getGuild()).addRole(event.getRole());
    }

    public void onRoleDelete(RoleDeleteEvent event){
        this.guildsSets.get(event.getGuild()).removeRole(event.getRole());
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