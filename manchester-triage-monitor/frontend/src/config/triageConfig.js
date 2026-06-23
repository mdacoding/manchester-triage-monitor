/**
 * Maps each TriageLevel to its clinical color accent and display label.
 *
 * Design principle: Colors are used as subtle left-border accents and status
 * dots — not as full-bleed background blocks — keeping the UI calm and readable.
 */
export const TRIAGE_CONFIG = {
  RED: {
    label:       'Sofort',
    sublabel:    'Lebensgefahr · 0 min',
    borderColor: 'border-l-red-500',
    dotColor:    'bg-red-500',
    textColor:   'text-red-600',
    badgeBg:     'bg-red-50',
    badgeText:   'text-red-700',
  },
  ORANGE: {
    label:       'Sehr dringend',
    sublabel:    'Max. 10 min',
    borderColor: 'border-l-orange-400',
    dotColor:    'bg-orange-400',
    textColor:   'text-orange-600',
    badgeBg:     'bg-orange-50',
    badgeText:   'text-orange-700',
  },
  YELLOW: {
    label:       'Dringend',
    sublabel:    'Max. 30 min',
    borderColor: 'border-l-yellow-400',
    dotColor:    'bg-yellow-400',
    textColor:   'text-yellow-600',
    badgeBg:     'bg-yellow-50',
    badgeText:   'text-yellow-700',
  },
  GREEN: {
    label:       'Normal',
    sublabel:    'Max. 90 min',
    borderColor: 'border-l-green-500',
    dotColor:    'bg-green-500',
    textColor:   'text-green-600',
    badgeBg:     'bg-green-50',
    badgeText:   'text-green-700',
  },
  BLUE: {
    label:       'Nicht dringend',
    sublabel:    'Max. 120 min',
    borderColor: 'border-l-blue-400',
    dotColor:    'bg-blue-400',
    textColor:   'text-blue-600',
    badgeBg:     'bg-blue-50',
    badgeText:   'text-blue-700',
  },
}
