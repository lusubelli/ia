import React, { Component } from 'react'
import Classification from './components/classification/classification'
import './App.css'

class App extends Component {

    constructor(props) {
        super(props)

        let colors = {
            "what": '#' + Math.floor(Math.random() * 16777215).toString(16),
            "unknown": '#' + Math.floor(Math.random() * 16777215).toString(16),
        }

        this.state = {

            intents: ["unknown"],
            names: ["unknown"],
            colors: colors,

            classification: {
                text: "Eteindre la lumiere",
                intentClassification: [
                    {
                        intent: "greetings",
                        probability: 1.0
                    }
                ],
                nameClassification: [
                    {
                        name: "what",
                        start: 0,
                        end: 8,
                        probability: 0.8
                    },
                    {
                        name: "what",
                        start: 12,
                        end: 19,
                        probability: 0.8
                    }
                ]
            }

        }
    }

    createIntent(intent) {
        let newIntents = [...this.state.intents]
        newIntents.push(intent)
        this.setState({
            ...this.state,
            intents: newIntents
        })
    }

    createName(name) {
        let newNames = [...this.state.names]
        newNames.push(name)
        let newColors = {...this.state.colors}
        newColors[name] = '#' + Math.floor(Math.random() * 16777215).toString(16)
        this.setState({
            ...this.state,
            names: newNames,
            colors: newColors
        })
    }

    render() {
        return (
        <div>
            <Classification
                colors={ this.state.colors }
                intents={ this.state.intents }
                names={ this.state.names }
                classification={ this.state.classification }
                createIntent={ this.createIntent.bind(this) }
                createName={ this.createName.bind(this) }>
            </Classification>
        </div>
        )
    }
}

export default App
