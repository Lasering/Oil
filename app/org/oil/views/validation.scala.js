@(fieldName: String, field: org.oil.Field[_])

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