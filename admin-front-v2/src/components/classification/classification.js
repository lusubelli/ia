import React from 'react'
import SelectInput from '../select-input/select-input'
import Highlight from '../highlight/highlight'

export default class Classification extends React.Component {

    constructor(props) {
        super(props)

        let classified = {
            text: props.classification.text,
            intent: props.classification.intentClassification[0].intent,
            names: props
                .classification
                .nameClassification
                .map( (nameClassification) => { return {
                    name: nameClassification.name,
                    start: nameClassification.start,
                    end: nameClassification.end
                } } )
        }

        let intentOptions = this.intentOptions(props.classification, props.intents)
        let nameOptions = this.nameOptions(props.classification, props.names)

        this.state = {
            classified: classified,
            intentOptions: intentOptions,
            nameOptions: nameOptions
        }
    }

    intentOptions(classification, intents) {
        return classification
           .intentClassification
           .map (intentClassification => {
               return {
                   key: intentClassification.intent,
                   value: (Math.round(intentClassification.probability * 100) / 100) * 100 + '% ' + intentClassification.intent
               }
           })
           .concat( intents.map(intent => {
               return {
                   key: intent,
                   value: intent
               }
           }))
    }

    nameOptions(classification, names) {
        return classification
            .nameClassification
            .map (nameClassification => {
                return {
                    key: nameClassification.name,
                    value: (Math.round(nameClassification.probability * 100) / 100) * 100 + '% ' + nameClassification.name
                }
            })
            .concat( names.map(name => {
                return {
                    key: name,
                    value: name
                }
            }))
            /*.filter((obj, pos, arr) => {
                return arr.map(mapObj => mapObj.name).indexOf(obj.name) === pos
            })*/
    }

    parts(text, names, colors) {

        var parts = []

        for (var i = 0; i < names.length; i++) {

            var name = names[i]

            if (i === 0 && name.start !== 0) {
                parts.push({
                    start: 0,
                    end: name.start,
                    color: null
                })
            }

            parts.push({
               start: name.start,
               end: name.end,
               color: colors[name.name]
            })

            if (i === names.length -1
                && name.end !== text.length) {
              parts.push({
                 start: name.end,
                 end: text.length,
                 color: null
              })
            }

            if (i < names.length -1/* && name.end +1 === names[i +1].start*/) {
              parts.push({
                 start: name.end,
                 end: names[i +1].start,
                 color: null
              })
            }

        }
        return parts
    }

    highlight(selection) {
        let newClassifiedNames = [...this.state.classified.names]
        newClassifiedNames.push({
            ...selection,
            name: 'unknown'
        })
        this.setState({
            ...this.state,
            classified: {
                ...this.state.classified,
                names: newClassifiedNames
            }
        })
    }

    selectIntent(intent) {
        let newState = {...this.state}
        newState.classified.intent = intent
        this.setState(newState)
    }

    createIntent(intent) {
        this.props.createIntent(intent)
        this.selectIntent(intent)
    }

    selectName(name, start, end) {
        let newClassifiedNames = [...this.state.classified.names]
        newClassifiedNames.filter( (name) => { return name.start === start && name.end === end } )[0].name = name
        this.setState({
            ...this.state,
            classified: {
                ...this.state.classified,
                names: newClassifiedNames
            }
        })
    }

    createName(name, start, end) {
        this.props.createName(name)
        this.selectName(name, start, end)
    }

    intentChoice(options) {
        let opts = options.filter((option) => { return option.key === this.state.classified.intent })
        let value = (opts && opts.length > 0) ? opts[0].value : this.state.classified.intent
        return (
        <SelectInput
            placeholder="Create new one"
            defaultValue={ options.key }
            options={ options }
            create={ this.createIntent.bind(this) }
            select={ this.selectIntent.bind(this) }>
            { value }
        </SelectInput>
        )
    }

    nameChoices(options) {
        return this.state
           .classified
           .names
           .map(name => {
               return this.nameChoice(options, name)
           })
    }

    nameChoice(options, name) {
        let opts = options.filter((option) => { return option.key === name.name })
        let value = (opts && opts.length > 0) ? opts[0].value : name.name
        return (
        <span key={ name.name + ":" + this.state.classified.text.substring(name.start, name.end) }>
            <br />
            <SelectInput
                placeholder="Create new one"
                defaultValue={ name.name }
                options={ options }
                create={ (event) => { this.createName(event, name.start, name.end) } }
                select={ (event) => { this.selectName(event, name.start, name.end) } }>
                { value }
            </SelectInput>: { this.state.classified.text.substring(name.start, name.end) }
        </span>
        )
    }

    highlightComponent() {
        let parts = this.parts(this.state.classified.text, this.state.classified.names, this.props.colors)
        return (
        <Highlight
            text={ this.state.classified.text }
            parts={ parts }
            select={ this.highlight.bind(this) }>
        </Highlight>
        )
    }

    render() {
        return (
        <div>
            { this.highlightComponent() }
            { this.intentChoice(this.state.intentOptions) }
            { this.nameChoices(this.state.nameOptions) }
            <div>
                <button>CANCEL</button>
                <button>SAVE</button>
                <button>REMOVE</button>
            </div>
        </div>
        )
    }

}