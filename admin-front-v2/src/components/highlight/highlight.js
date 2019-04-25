import React from 'react'

export default class Highlight extends React.Component {

    select() {
        const selection = window.getSelection().getRangeAt(0)

        let spans = selection.startContainer.parentElement.parentNode.children
        let lengthBeforeStart = this.lengthBeforeSpan(spans, selection.startContainer.parentElement)
        let lengthBeforeEnd = this.lengthBeforeSpan(spans, selection.endContainer.parentElement)

        let start = lengthBeforeStart + selection.startOffset
        let end = lengthBeforeEnd + selection.endOffset

        this.props.select({
            start: start,
            end: end
        })
    }

    lengthBeforeSpan(spans, span) {
        var length = 0
        for (var i= 0; i < spans.length; i++){
            if (spans[i].tagName.toLowerCase().includes('span')) {
                if (span.textContent === spans[i].textContent) {
                    break
                }
            }
            length += spans[i].textContent.length
        }
        return length
    }

    render() {
        return (
            <div onMouseUp={ this.select.bind(this) }>
                {
                    this.props.parts.map((part, index) => {
                        return (
                        <span key={index} style={{backgroundColor: part.color}}>{ this.props.text.substring(part.start, part.end) }</span>
                        )
                    })
                }
            </div>
        )
    }

}