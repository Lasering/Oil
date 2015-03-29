@(form: org.oil.Form[_, _])

@* 1) Iterate through the list of all input providers in the fields and
add to the page all the includes *@

@* 2) Iterate through the list of all constraints in the fields and
add to the page all the custom validation methods *@

@* 3) Iterate through the list of all constraints in the fields and
add to the page all the rules to the jQuery Validation *@

@* 3) Iterate through the list of all input providers in the fields and
add to the page all the onReady *@


@*

  /*$("#myinput").rules( "add", {
    required: true,
    minlength: 2,
    messages: {
      required: "Required input",
      minlength: jQuery.format("Please, at least {0} characters are necessary")
    }
  });*/

$("#@fieldName").rules("add", {
    @field.constraints.map{ case (restraintName, args) =>
        @restraintName match {
            case "constraint.required" => { required: true, }
            case "constraint.email" => { email: true, }
            case _ => { @restraintName.replaceFirst("constraint.", "").toLowerCase(): @args.mkString, }
        }
    }
    messages: {
        @field.constraints.map{ case (restraintName, args) =>
            @restraintName.replaceFirst("constraint.", "").toLowerCase():
            @if(Messages.isDefinedAt(name + "." + restraintName.replaceFirst("constraint.", "error."))){
                $.format("@Messages(name + "." + restraintName.replaceFirst("constraint.", "error."))"),
            } else {
                $.format("@Messages(restraintName.replaceFirst("constraint.", "error."))"),
            }
        }
    }
});
*@