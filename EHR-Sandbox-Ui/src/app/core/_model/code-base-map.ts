import { BaseFormOption } from "./structure";

/**
 * Interface for codemaps lists
 */
export interface CodeBaseMap {
  [key: string]: CodeMap;
}
export interface CodeMap {
  [key: string]: Code;
}
export interface Code {
  "value": string;
  "label": string;
  "description"?: string;
  "codeStatus"?: string;
  "reference"?: CodeReference;
  "useDate"?: Date;
  "useAge"?: string;
  "conceptType"?: string;
  "testAge"?: string;
}
export interface CodeReference {
  linkTo: CodeReferenceLink[];
}

export interface CodeReferenceLink {
  value: string;
  codeset: string;
}

export interface CodeReferenceTable {
  [origin_label: string]: CodeReferenceTableMember
}
export interface CodeReferenceTableMember {
  reference: CodeReference;
  value: string;
}
